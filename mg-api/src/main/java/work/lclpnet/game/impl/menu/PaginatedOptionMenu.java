package work.lclpnet.game.impl.menu;

import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.game.api.option.menu.IconDecorator;
import work.lclpnet.game.api.option.menu.OptionSort;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.network.CustomClickActionCallback;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.kibu.inv.item.ItemStackUtil;
import work.lclpnet.kibu.inv.prompt.OptionPrompt;
import work.lclpnet.kibu.inv.type.RestrictedInventory;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.text.RootText;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A reusable, configurable chest menu that displays a collection of options across multiple pages.
 * <p>
 * When the header bar is active the first row holds the navigation controls (previous / search / page
 * info / sort / next) and the remaining five rows show the options of the current page. The header is
 * enabled automatically when search or sort is requested, or when the options do not fit a single
 * screen. Otherwise, the menu degrades to a simple, single page layout sized to the option count.
 * <p>
 * Clicks are dispatched to the per-player inventory via {@link OptionPrompt.Handler}, so the menu only
 * registers the {@link CustomClickActionCallback} (for the search dialog) and a quit listener itself.
 *
 * @param <T> The option type.
 */
public final class PaginatedOptionMenu<T> {

    private static final int ROWS = 6;
    private static final int HEADER_SIZE = 9;
    private static final int PAGE_SIZE = (ROWS - 1) * 9;
    private static final int SIMPLE_CAPACITY = ROWS * 9;

    private static final int SLOT_PREV = 0;
    private static final int SLOT_SEARCH = 2;
    private static final int SLOT_PAGE = 4;
    private static final int SLOT_SORT = 6;
    private static final int SLOT_NEXT = 8;

    private final Translations translations;
    private final Identifier submitId;
    private final Identifier cancelId;
    private final Function<ServerPlayer, Component> title;
    private final BiFunction<ServerPlayer, T, ItemStack> optionIcon;
    private final IconDecorator<T> decorator;
    private final BiFunction<ServerPlayer, T, String> searchText;
    private final @Nullable Boolean headerOverride;
    private final boolean searchEnabled;
    private final boolean sortEnabled;
    private final List<OptionSort<T>> sortOptions;
    private final BiConsumer<ServerPlayer, T> onSelect;
    private final Predicate<ServerPlayer> canInteract;
    private final boolean closeOnSelect;

    private final Map<UUID, View> views = new HashMap<>();
    private volatile Collection<T> options;

    private PaginatedOptionMenu(Builder<T> builder) {
        this.translations = builder.translations;
        this.title = builder.title;
        this.options = builder.options;
        this.optionIcon = builder.optionIcon;
        this.decorator = builder.decorator;
        this.searchText = builder.searchText;
        this.headerOverride = builder.headerOverride;
        this.searchEnabled = builder.searchEnabled;
        this.sortEnabled = builder.sortEnabled;
        this.sortOptions = builder.sortOptions;
        this.onSelect = builder.onSelect;
        this.canInteract = builder.canInteract;
        this.closeOnSelect = builder.closeOnSelect;

        Identifier id = builder.id;
        this.submitId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/search_submit");
        this.cancelId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/search_cancel");
    }

    public static <T> Builder<T> builder(Translations translations, Identifier id) {
        return new Builder<>(translations, id);
    }

    /**
     * Register the hooks required by this menu. Must be called once before the menu is opened.
     *
     * @param hooks The hook registrar of the surrounding activity.
     */
    public void init(HookRegistrar hooks) {
        if (searchEnabled) {
            CustomClickActionCallback.HOOK.registerWith(hooks, this::onCustomClick);
        }

        PlayerConnectionHooks.QUIT.registerWith(hooks, player -> views.remove(player.getUUID()));
    }

    /**
     * Open the menu for a player, creating their view on first open.
     */
    public void open(ServerPlayer player) {
        View view = views.computeIfAbsent(player.getUUID(), uuid -> new View(player));
        render(view);
        view.inventory.open(player);
    }

    /**
     * Re-render the menu for every player that currently has it open. Use after the underlying option
     * state changes (e.g. a new vote came in).
     */
    public void refresh() {
        for (View view : views.values()) {
            var menu = view.player.containerMenu;

            if (menu instanceof ChestMenu chestMenu && chestMenu.getContainer() == view.inventory) {
                render(view);
            }
        }
    }

    /**
     * Replace the options shown by this menu and refresh any open views.
     */
    public void setOptions(Collection<T> options) {
        this.options = options;
        refresh();
    }

    private void onCustomClick(ServerPlayer player, Identifier id, Optional<Tag> payload) {
        View view = views.get(player.getUUID());
        if (view == null) return;

        if (id.equals(submitId)) {
            view.search = compound(payload).getStringOr(MenuSearchDialog.SEARCH_KEY, "");
            view.page = 0;
            reopen(view);
        } else if (id.equals(cancelId)) {
            reopen(view);
        }
    }

    private void reopen(View view) {
        render(view);
        view.inventory.open(view.player);
    }

    private void render(View view) {
        if (view.headerActive) {
            renderPaged(view);
        } else {
            renderSimple(view);
        }
    }

    private void renderPaged(View view) {
        ServerPlayer player = view.player;
        List<T> all = filterAndSort(player, view);
        int pageCount = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));

        view.page = Math.clamp(view.page, 0, pageCount - 1);

        paintHeader(view, all.size(), pageCount);

        int start = view.page * PAGE_SIZE;
        view.pageItems = new ArrayList<>(all.subList(start, Math.min(all.size(), start + PAGE_SIZE)));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int slot = HEADER_SIZE + i;

            if (i < view.pageItems.size()) {
                view.inventory.setItem(slot, icon(player, view.pageItems.get(i)));
            } else {
                view.inventory.setItem(slot, ItemStack.EMPTY);
            }
        }

        if (all.isEmpty() && searchEnabled && !view.search.isBlank()) {
            view.inventory.setItem(HEADER_SIZE + PAGE_SIZE / 2, noResultsItem(player));
        }
    }

    private void renderSimple(View view) {
        ServerPlayer player = view.player;
        List<T> all = List.copyOf(options);
        view.pageItems = all;

        int capacity = view.inventory.getContainerSize();

        for (int i = 0; i < capacity; i++) {
            if (i < all.size()) {
                view.inventory.setItem(i, icon(player, all.get(i)));
            } else {
                view.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void paintHeader(View view, int total, int pageCount) {
        RestrictedInventory inv = view.inventory;
        ServerPlayer player = view.player;

        for (int slot = 0; slot < HEADER_SIZE; slot++) inv.setItem(slot, filler());

        if (view.page > 0) inv.setItem(SLOT_PREV, prevPageItem(player));
        if (view.page < pageCount - 1) inv.setItem(SLOT_NEXT, nextPageItem(player));

        if (searchEnabled) inv.setItem(SLOT_SEARCH, searchItem(player, view));
        inv.setItem(SLOT_PAGE, pageItem(player, view, total, pageCount));
        if (sortEnabled) inv.setItem(SLOT_SORT, sortItem(player, view));
    }

    private void handleClick(PlayerInventoryHooks.ClickEvent event) {
        ServerPlayer player = event.player();
        View view = views.get(player.getUUID());
        if (view == null) return;

        Slot slot = event.handlerSlot();
        if (slot == null) return;

        int index = slot.getContainerSlot();
        if (index < 0 || index >= view.inventory.getContainerSize()) return;

        if (view.headerActive) {
            if (index < HEADER_SIZE) {
                handleHeaderClick(view, index, event.button());
                return;
            }

            select(view, index - HEADER_SIZE);
        } else {
            select(view, index);
        }
    }

    private void handleHeaderClick(View view, int index, int button) {
        ServerPlayer player = view.player;

        switch (index) {
            case SLOT_PREV -> {
                if (view.page > 0) {
                    view.page--;
                    render(view);
                }
            }
            case SLOT_NEXT -> {
                view.page++;
                render(view);
            }
            case SLOT_SEARCH -> {
                if (searchEnabled) {
                    MenuSearchDialog.open(player, translations, view.search, submitId, cancelId);
                }
            }
            case SLOT_SORT -> {
                if (sortEnabled && !sortOptions.isEmpty()) {
                    int n = sortOptions.size();
                    view.sortIndex = button == 1 ? Math.floorMod(view.sortIndex - 1, n) : Math.floorMod(view.sortIndex + 1, n);
                    view.page = 0;
                    render(view);
                }
            }
            default -> {}
        }
    }

    private void select(View view, int optionIndex) {
        ServerPlayer player = view.player;

        if (optionIndex < 0 || optionIndex >= view.pageItems.size()) return;
        if (!canInteract.test(player)) return;

        T option = view.pageItems.get(optionIndex);

        onSelect.accept(player, option);

        if (closeOnSelect) {
            player.closeContainer();
        }
    }

    private List<T> filterAndSort(ServerPlayer player, View view) {
        List<T> base = new ArrayList<>(options);

        String search = view.search == null ? "" : view.search.trim();
        List<T> filtered;

        if (search.isEmpty()) {
            filtered = base;
        } else {
            String needle = search.toLowerCase();
            filtered = base.stream()
                    .filter(option -> searchText.apply(player, option).toLowerCase().contains(needle))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (sortEnabled && !sortOptions.isEmpty()) {
            return currentSort(view).sort(filtered, option -> searchText.apply(player, option));
        }

        return filtered;
    }

    private OptionSort<T> currentSort(View view) {
        return sortOptions.get(Math.floorMod(view.sortIndex, sortOptions.size()));
    }

    private ItemStack icon(ServerPlayer player, T option) {
        ItemStack base = optionIcon.apply(player, option);
        return decorator.decorate(player, option, base);
    }

    private boolean useHeader() {
        if (options.size() > SIMPLE_CAPACITY) return true;
        if (headerOverride != null) return headerOverride;
        return searchEnabled || sortEnabled;
    }

    private ItemStack filler() {
        ItemStack stack = new ItemStack(Items.STAINED_GLASS_PANE.black());
        stack.set(DataComponents.ITEM_NAME, Component.empty());
        stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
        return stack;
    }

    private ItemStack prevPageItem(ServerPlayer player) {
        ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
        stack.set(DataComponents.ITEM_NAME, label(player, "mg-api.menu.prev").withStyle(ChatFormatting.YELLOW));
        return stack;
    }

    private ItemStack nextPageItem(ServerPlayer player) {
        ItemStack stack = new ItemStack(Items.EMERALD_BLOCK);
        stack.set(DataComponents.ITEM_NAME, label(player, "mg-api.menu.next").withStyle(ChatFormatting.YELLOW));
        return stack;
    }

    private ItemStack searchItem(ServerPlayer player, View view) {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        stack.set(DataComponents.ITEM_NAME, label(player, "mg-api.menu.search").withStyle(ChatFormatting.YELLOW));

        Component line;
        if (!view.search.isBlank()) {
            line = Component.literal("\"" + view.search + "\"")
                    .withStyle(style -> style.withItalic(false).applyFormat(ChatFormatting.AQUA));
        } else {
            line = label(player, "mg-api.menu.search_hint")
                    .withStyle(style -> style.withItalic(false).applyFormat(ChatFormatting.GRAY));
        }

        ItemStackUtil.setLore(stack, List.of(line));

        return stack;
    }

    private ItemStack pageItem(ServerPlayer player, View view, int total, int pageCount) {
        ItemStack stack = new ItemStack(Items.PAPER);

        stack.set(DataComponents.ITEM_NAME, label(player, "mg-api.menu.page", view.page + 1, pageCount)
                .withStyle(style -> style.withItalic(false).applyFormat(ChatFormatting.YELLOW)));

        Component lore = label(player, "mg-api.menu.total", total)
                .withStyle(style -> style.withItalic(false).applyFormat(ChatFormatting.GRAY));

        ItemStackUtil.setLore(stack, List.of(lore));

        return stack;
    }

    private ItemStack sortItem(ServerPlayer player, View view) {
        ItemStack stack = new ItemStack(Items.HOPPER);
        String sortName = translations.translate(player, currentSort(view).labelKey());

        stack.set(DataComponents.ITEM_NAME, label(player, "mg-api.menu.sort", sortName)
                .withStyle(style -> style.withItalic(false).applyFormat(ChatFormatting.YELLOW)));

        Component lore = label(player, "mg-api.menu.sort_hint")
                .withStyle(style -> style.withItalic(false).applyFormat(ChatFormatting.GRAY));

        ItemStackUtil.setLore(stack, List.of(lore));

        return stack;
    }

    private ItemStack noResultsItem(ServerPlayer player) {
        ItemStack stack = new ItemStack(Items.BARRIER);
        stack.set(DataComponents.ITEM_NAME, label(player, "mg-api.menu.no_results").withStyle(ChatFormatting.RED));
        return stack;
    }

    private RootText label(ServerPlayer player, String key, Object... args) {
        return translations.translateText(player, key, args);
    }

    private static CompoundTag compound(Optional<Tag> payload) {
        Tag tag = payload.orElse(null);
        return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }

    private final class View {
        final ServerPlayer player;
        final MenuInventory inventory;
        final boolean headerActive;
        String search = "";
        int sortIndex = 0;
        int page = 0;
        List<T> pageItems = List.of();

        View(ServerPlayer player) {
            this.player = player;
            this.headerActive = useHeader();

            int rows = headerActive
                    ? ROWS
                    : Math.clamp((int) Math.ceil(Math.max(1, options.size()) / 9d), 1, ROWS);

            this.inventory = new MenuInventory(rows, title.apply(player));
        }
    }

    private final class MenuInventory extends RestrictedInventory implements OptionPrompt.Handler {
        MenuInventory(int rows, Component title) {
            super(rows, title);
        }

        @Override
        public void onClick(PlayerInventoryHooks.ClickEvent event) {
            handleClick(event);
        }
    }

    public static final class Builder<T> {
        private final Translations translations;
        private final Identifier id;
        private Function<ServerPlayer, Component> title = _ -> Component.empty();
        private Collection<T> options = List.of();
        private BiFunction<ServerPlayer, T, ItemStack> optionIcon;
        private IconDecorator<T> decorator = IconDecorator.none();
        private BiFunction<ServerPlayer, T, String> searchText = null;
        private @Nullable Boolean headerOverride = null;
        private boolean searchEnabled = false;
        private boolean sortEnabled = false;
        private List<OptionSort<T>> sortOptions = OptionSort.defaults();
        private BiConsumer<ServerPlayer, T> onSelect = (_, _) -> {};
        private Predicate<ServerPlayer> canInteract = _ -> true;
        private boolean closeOnSelect = false;

        private Builder(Translations translations, Identifier id) {
            this.translations = Objects.requireNonNull(translations, "translations");
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder<T> title(Function<ServerPlayer, Component> title) {
            this.title = Objects.requireNonNull(title);
            return this;
        }

        public Builder<T> options(Collection<T> options) {
            this.options = Objects.requireNonNull(options);
            return this;
        }

        public Builder<T> optionIcon(BiFunction<ServerPlayer, T, ItemStack> optionIcon) {
            this.optionIcon = Objects.requireNonNull(optionIcon);
            return this;
        }

        public Builder<T> decorator(IconDecorator<T> decorator) {
            this.decorator = Objects.requireNonNull(decorator);
            return this;
        }

        public Builder<T> searchText(BiFunction<ServerPlayer, T, String> searchText) {
            this.searchText = Objects.requireNonNull(searchText);
            return this;
        }

        /**
         * Force the header bar on ({@code true}) or off ({@code false}). When {@code null} (default) the
         * header is shown automatically if search or sort is enabled. It is always shown when the options
         * overflow a single page, regardless of this setting.
         */
        public Builder<T> header(@Nullable Boolean header) {
            this.headerOverride = header;
            return this;
        }

        public Builder<T> search(boolean searchEnabled) {
            this.searchEnabled = searchEnabled;
            return this;
        }

        public Builder<T> sort(boolean sortEnabled) {
            this.sortEnabled = sortEnabled;
            return this;
        }

        public Builder<T> sort(boolean sortEnabled, List<OptionSort<T>> sortOptions) {
            this.sortEnabled = sortEnabled;
            this.sortOptions = Objects.requireNonNull(sortOptions);
            return this;
        }

        public Builder<T> onSelect(BiConsumer<ServerPlayer, T> onSelect) {
            this.onSelect = Objects.requireNonNull(onSelect);
            return this;
        }

        public Builder<T> canInteract(Predicate<ServerPlayer> canInteract) {
            this.canInteract = Objects.requireNonNull(canInteract);
            return this;
        }

        public Builder<T> closeOnSelect(boolean closeOnSelect) {
            this.closeOnSelect = closeOnSelect;
            return this;
        }

        public PaginatedOptionMenu<T> build() {
            Objects.requireNonNull(optionIcon, "optionIcon must be set");

            if (searchText == null) {
                BiFunction<ServerPlayer, T, ItemStack> icon = optionIcon;
                searchText = (player, option) -> icon.apply(player, option).getHoverName().getString();
            }

            return new PaginatedOptionMenu<>(this);
        }
    }
}
