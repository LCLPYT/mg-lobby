package work.lclpnet.game.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.game.api.option.OptionVoting;
import work.lclpnet.game.api.option.VoteResult;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.inv.item.ItemStackUtil;
import work.lclpnet.kibu.inv.prompt.OptionPrompt;
import work.lclpnet.kibu.inv.type.RestrictedInventory;
import work.lclpnet.kibu.translate.Translations;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.summingInt;
import static net.minecraft.ChatFormatting.*;
import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class Voting<T> {

    private final String id;
    private final OptionVoting<T> data;
    private final Translations translations;
    private final Map<UUID, T> votes = new HashMap<>();
    private final Map<UUID, OpenView> openPrompts = new HashMap<>();
    private final boolean showVoteCount;

    private boolean open = true;

    public Voting(String id, OptionVoting<T> data, Translations translations) {
        this(id, data, translations, true);
    }

    public Voting(String id, OptionVoting<T> data, Translations translations, boolean showVoteCount) {
        this.id = id;
        this.data = data;
        this.translations = translations;
        this.showVoteCount = showVoteCount;
    }

    public String getId() {
        return id;
    }

    public OptionVoting<T> getData() {
        return data;
    }

    public void open(ServerPlayer player) {
        T currentVote;
        VoteResult<T> current;

        synchronized (this) {
            if (!open) return;

            currentVote = votes.getOrDefault(player.getUUID(), null);
            current = getCurrentResult();
        }

        Component title = data.title().apply(player);

        var handle = OptionPrompt.openHandle(player, title, data.options(),
                opt -> getIcon(player, opt, opt.equals(currentVote), current.votes(opt)));

        UUID uuid = player.getUUID();

        synchronized (this) {
            openPrompts.put(uuid, new OpenView(player, handle.inventory()));
        }

        handle.future().whenComplete((selected, err) -> {
            synchronized (this) {
                openPrompts.remove(uuid);
            }

            if (selected != null && err == null) {
                selected.ifPresent(opt -> vote(player, opt));
            }
        });
    }

    private ItemStack getIcon(ServerPlayer player, T option, boolean selected, int votes) {
        ItemStack icon = data.optionIcons().apply(player, option);

        icon.setCount(max(1, votes));

        List<Component> lore = icon.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
        List<Component> newLore = new ArrayList<>();

        if (showVoteCount) {
            newLore.add(translations.translateText(player, "mg-api.voting.votes", styled(votes, YELLOW)).withStyle(GREEN));
        }

        if (selected) {
            icon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            newLore.add(translations.translateText(player, "mg-api.voting.selected").withStyle(AQUA));
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

        refreshOpenPrompts();
    }

    public void removeVote(ServerPlayer player) {
        synchronized (this) {
            if (!open) return;

            votes.remove(player.getUUID());
        }

        refreshOpenPrompts();
    }

    private void refreshOpenPrompts() {
        List<OpenView> views;
        VoteResult<T> result;
        Map<UUID, T> votesSnapshot;

        synchronized (this) {
            if (openPrompts.isEmpty()) return;

            views = List.copyOf(openPrompts.values());
            result = getCurrentResult();
            votesSnapshot = Map.copyOf(votes);
        }

        for (OpenView view : views) {
            ServerPlayer viewer = view.player();
            T viewerVote = votesSnapshot.get(viewer.getUUID());
            int i = 0;

            for (T opt : data.options()) {
                view.inventory().setItem(i++, getIcon(viewer, opt, opt.equals(viewerVote), result.votes(opt)));
            }
        }
    }

    private record OpenView(ServerPlayer player, RestrictedInventory inventory) {}

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
