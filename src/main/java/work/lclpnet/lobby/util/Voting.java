package work.lclpnet.lobby.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.kibu.inv.item.ItemStackUtil;
import work.lclpnet.kibu.inv.prompt.OptionPrompt;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.game.api.option.OptionVoting;
import work.lclpnet.lobby.game.api.option.VoteResult;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.summingInt;
import static net.minecraft.util.Formatting.*;
import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class Voting<T> implements Interactable {

    private final String id;
    private final OptionVoting<T> data;
    private final Translations translations;
    private final Map<UUID, T> votes = new HashMap<>();
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

    @Override
    public void onInteract(ServerPlayerEntity player) {
        T currentVote;
        VoteResult<T> current;

        synchronized (this) {
            if (!open) return;

            currentVote = votes.getOrDefault(player.getUuid(), null);
            current = currentResult();
        }

        Text title = data.title().apply(player);

        OptionPrompt.open(player, title, data.options(), opt -> getIcon(player, opt, opt.equals(currentVote), current.votes(opt)))
                .thenAccept(selected -> selected.ifPresent(opt -> vote(player, opt)));
    }

    private ItemStack getIcon(ServerPlayerEntity player, T option, boolean selected, int votes) {
        ItemStack icon = data.optionIcons().apply(player, option);

        List<Text> lore = icon.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();
        List<Text> newLore = new ArrayList<>();

        if (showVoteCount) {
            newLore.add(translations.translateText(player, "lobby.voting.votes", styled(votes, YELLOW)).formatted(GREEN));
        }

        if (selected) {
            icon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

            newLore.add(translations.translateText(player, "lobby.voting.selected").formatted(AQUA));
        }

        if (!newLore.isEmpty()) {
            // newline if there is already lore
            if (!lore.isEmpty()) {
                newLore.addFirst(Text.empty());
                newLore.addAll(0, lore);
            }

            ItemStackUtil.setLore(icon, newLore);
        }

        icon.set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT
                .with(DataComponentTypes.ATTRIBUTE_MODIFIERS, true)
                .with(DataComponentTypes.UNBREAKABLE, true)
                .with(DataComponentTypes.ENCHANTMENTS, true)
                .with(DataComponentTypes.DAMAGE, true));

        return icon;
    }

    public void vote(ServerPlayerEntity player, T option) {
        synchronized (this) {
            if (!open) return;

            votes.put(player.getUuid(), option);
        }

        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.RECORDS, 0.5f, 1f);

        Text name = data.optionName().apply(player, option);

        player.sendMessage(translations.translateText(player, "lobby.voting.voted_for", styled(name, YELLOW)).formatted(GREEN));
    }

    public synchronized VoteResult<T> end() {
        open = false;

        return currentResult();
    }

    public synchronized @NotNull VoteResult<T> currentResult() {
        Map<T, Integer> resultMap = votes.values().stream()
                .collect(Collectors.groupingBy(identity(), summingInt(e -> 1)));

        for (T option : data.options()) {
            resultMap.putIfAbsent(option, 0);
        }

        return () -> resultMap;
    }
}
