package work.lclpnet.lobby.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import work.lclpnet.kibu.inv.prompt.OptionPrompt;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.game.api.option.OptionVoting;
import work.lclpnet.lobby.game.api.option.VoteResult;

import java.util.*;
import java.util.function.Predicate;
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
    private boolean open = true;

    public Voting(String id, OptionVoting<T> data, Translations translations) {
        this.id = id;
        this.data = data;
        this.translations = translations;
    }

    public String getId() {
        return id;
    }

    public OptionVoting<T> getData() {
        return data;
    }

    @Override
    public synchronized void onInteract(ServerPlayerEntity player) {
        if (!open) return;

        T currentVote = votes.getOrDefault(player.getUuid(), null);

        Text title = data.title().apply(player);

        OptionPrompt.open(player, title, data.options(), opt -> getIcon(player, opt, vote -> vote.equals(currentVote)))
                .thenAccept(selected -> selected.ifPresent(opt -> vote(player, opt)));
    }

    private ItemStack getIcon(ServerPlayerEntity player, T option, Predicate<T> selected) {
        ItemStack icon = data.optionIcons().apply(player, option);

        if (selected.test(option)) {
            icon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

            List<Text> lore = icon.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();
            List<Text> newLore = new ArrayList<>(lore.isEmpty() ? 1 : lore.size() + 2);

            newLore.addAll(lore);

            // newline if there is already lore
            if (!lore.isEmpty()) {
                newLore.add(Text.empty());
            }

            newLore.add(translations.translateText(player, "lobby.voting.selected").formatted(AQUA));

            icon.set(DataComponentTypes.LORE, new LoreComponent(newLore));
        }

        icon.set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT
                .with(DataComponentTypes.ATTRIBUTE_MODIFIERS, true)
                .with(DataComponentTypes.UNBREAKABLE, true)
                .with(DataComponentTypes.ENCHANTMENTS, true)
                .with(DataComponentTypes.DAMAGE, true));

        return icon;
    }

    public synchronized void vote(ServerPlayerEntity player, T option) {
        if (!open) return;

        votes.put(player.getUuid(), option);

        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.RECORDS, 0.5f, 1f);

        Text name = data.optionName().apply(player, option);

        player.sendMessage(translations.translateText(player, "lobby.voting.voted_for", styled(name, YELLOW)).formatted(GREEN));
    }

    public synchronized VoteResult<T> end() {
        open = false;

        Map<T, Integer> resultMap = votes.values().stream()
                .collect(Collectors.groupingBy(identity(), summingInt(e -> 1)));

        for (T option : data.options()) {
            resultMap.putIfAbsent(option, 0);
        }

        return () -> resultMap;
    }
}
