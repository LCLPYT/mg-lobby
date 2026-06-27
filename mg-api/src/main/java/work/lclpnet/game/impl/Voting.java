package work.lclpnet.game.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.game.api.option.OptionVoting;
import work.lclpnet.game.api.option.VoteResult;
import work.lclpnet.game.impl.menu.PaginatedOptionMenu;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.inv.item.ItemStackUtil;
import work.lclpnet.kibu.translate.Translations;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.summingInt;
import static net.minecraft.ChatFormatting.*;
import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class Voting<T> {

    private final String id;
    private final OptionVoting<T> data;
    private final Translations translations;
    private final Map<UUID, T> votes = new HashMap<>();
    private final boolean showVoteCount;
    private final PaginatedOptionMenu<T> menu;

    private boolean open = true;

    public Voting(String id, OptionVoting<T> data, Translations translations) {
        this(id, data, translations, true);
    }

    public Voting(String id, OptionVoting<T> data, Translations translations, boolean showVoteCount) {
        this(id, data, translations, showVoteCount, false, false);
    }

    public Voting(String id, OptionVoting<T> data, Translations translations, boolean showVoteCount, boolean search, boolean sort) {
        this.id = id;
        this.data = data;
        this.translations = translations;
        this.showVoteCount = showVoteCount;

        this.menu = PaginatedOptionMenu.<T>builder(translations, Identifier.fromNamespaceAndPath("mg-api", "voting/" + id))
                .title(data.title())
                .options(data.options())
                .optionIcon(data.optionIcons())
                .decorator(this::decorate)
                .searchText((player, option) -> data.optionName().apply(player, option).getString())
                .search(search)
                .sort(sort)
                .onSelect(this::vote)
                .closeOnSelect(true)
                .build();
    }

    public String getId() {
        return id;
    }

    public OptionVoting<T> getData() {
        return data;
    }

    /**
     * Register the hooks required by the voting menu. Called by
     * {@link work.lclpnet.game.util.GameStartUtil#setupVoting}.
     *
     * @param hooks The hook registrar of the surrounding activity.
     */
    public void init(HookRegistrar hooks) {
        menu.init(hooks);
    }

    public void open(ServerPlayer player) {
        synchronized (this) {
            if (!open) return;
        }

        menu.open(player);
    }

    private ItemStack decorate(ServerPlayer player, T option, ItemStack icon) {
        int voteCount = getCurrentResult().votes(option);

        T currentVote;
        synchronized (this) {
            currentVote = votes.get(player.getUUID());
        }

        boolean selected = option.equals(currentVote);

        icon.setCount(Math.clamp(voteCount, 1, icon.getMaxStackSize()));

        List<Component> lore = icon.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
        List<Component> newLore = new ArrayList<>();

        if (showVoteCount) {
            newLore.add(translations.translateText(player, "mg-api.voting.votes", styled(voteCount, YELLOW)).withStyle(GREEN));
        }

        if (voteCount > 0) {
            icon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        if (selected) {
            newLore.add(translations.translateText(player, "mg-api.voting.your_vote").withStyle(AQUA));
        }

        if (!newLore.isEmpty()) {
            // newline if there is already lore
            if (!lore.isEmpty()) {
                newLore.addFirst(Component.empty());
                newLore.addAll(0, lore);
            }

            ItemStackUtil.setLore(icon, newLore);
        }

        icon.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT
                .withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
                .withHidden(DataComponents.UNBREAKABLE, true)
                .withHidden(DataComponents.ENCHANTMENTS, true)
                .withHidden(DataComponents.DAMAGE, true)
                .withHidden(DataComponents.POTION_CONTENTS, true)
                .withHidden(DataComponents.TRIM, true)
                .withHidden(DataComponents.DYED_COLOR, true));

        return icon;
    }

    public void vote(ServerPlayer player, T option) {
        synchronized (this) {
            if (!open) return;

            votes.put(player.getUUID(), option);
        }

        ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.ENDER_DRAGON_HURT, SoundSource.RECORDS, 0.4f, 1f);

        Component name = data.optionName().apply(player, option);

        player.sendSystemMessage(translations.translateText(player, "mg-api.voting.voted_for", styled(name, YELLOW)).withStyle(GREEN));

        menu.refresh();
    }

    public void removeVote(ServerPlayer player) {
        synchronized (this) {
            if (!open) return;

            votes.remove(player.getUUID());
        }

        menu.refresh();
    }

    public synchronized VoteResult<T> end() {
        open = false;

        return getCurrentResult();
    }

    public synchronized @NotNull VoteResult<T> getCurrentResult() {
        Map<T, Integer> resultMap = votes.values().stream()
                .collect(Collectors.groupingBy(identity(), summingInt(e -> 1)));

        for (T option : data.options()) {
            resultMap.putIfAbsent(option, 0);
        }

        return () -> resultMap;
    }

    public synchronized Set<UUID> getVoters() {
        return Set.copyOf(votes.keySet());
    }

    public boolean hasVoted(ServerPlayer player) {
        return hasVoted(player.getUUID());
    }

    public synchronized boolean hasVoted(UUID uuid) {
        return votes.containsKey(uuid);
    }
}
