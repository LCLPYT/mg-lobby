package work.lclpnet.lobby.game.util;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.SchedulerAction;
import work.lclpnet.kibu.scheduler.api.TaskHandle;
import work.lclpnet.kibu.scheduler.api.TaskScheduler;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.bossbar.BossBarProvider;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class BossBarTimer implements SchedulerAction {

    private final Translations translations;
    private final ResourceLocation id;
    private final Object subject;
    private final boolean cycleColor;
    private final boolean alertSound;
    private final int durationTicks;
    private final List<Runnable> whenDone = new ArrayList<>();
    private volatile List<ServerPlayer> players = null;
    private volatile TranslatedBossBar bossBar;
    private TaskHandle taskHandle;
    private boolean paused = false;
    private boolean wasPaused = false;
    private int colorIndex;
    private int timer;

    private BossBarTimer(Translations translations, ResourceLocation id, Object subject, boolean cycleColor,
                         boolean alertSound, int durationTicks, BossEvent.BossBarColor color) {
        this.translations = translations;
        this.id = id;
        this.subject = subject;
        this.cycleColor = cycleColor;
        this.alertSound = alertSound;
        this.durationTicks = durationTicks;
        this.colorIndex = color.ordinal();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void stop() {
        if (bossBar.isVisible()) {
            bossBar.setVisible(false);
        }

        if (taskHandle == null) return;

        taskHandle.cancel();
        taskHandle = null;
    }

    public TranslatedBossBar getBossBar() {
        return bossBar;
    }

    public void whenDone(Runnable action) {
        whenDone.add(action);
    }

    public void start(BossBarProvider bossBarProvider, TaskScheduler scheduler) {
        synchronized (this) {
            timer = durationTicks;

            var translation = titleTranslation();

            bossBar = translations.translateBossBar(id, translation.left(), translation.right())
                    .with(bossBarProvider).formatted(ChatFormatting.YELLOW);

            bossBar.setColor(BossEvent.BossBarColor.values()[colorIndex]);
            bossBar.setProgress(1f);

            if (this.players != null) {
                bossBar.addPlayers(this.players);
                this.players = null;
            }

            taskHandle = scheduler.interval(this, 1).whenComplete(() -> {
                bossBar.setVisible(false);
                whenDone.forEach(Runnable::run);
            });
        }
    }

    private Pair<String, Object[]> titleTranslation() {
        if (paused) {
            return Pair.of("lobby.countdown.title.paused", new Object[] {
                    styled(subject, ChatFormatting.AQUA, ChatFormatting.BOLD)
            });
        }

        int seconds = timer / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return Pair.of("lobby.countdown.title.minutes", new Object[] {
                    styled(subject, ChatFormatting.AQUA, ChatFormatting.BOLD),
                    minutes,
                    seconds
            });
        }

        return Pair.of("lobby.countdown.title.seconds", new Object[] {
                styled(subject, ChatFormatting.AQUA, ChatFormatting.BOLD),
                seconds
        });
    }

    private void updateBossBar() {
        if (cycleColor) {
            colorIndex = (colorIndex + 1) % BossEvent.BossBarColor.values().length;
        }

        var titleTranslation = titleTranslation();

        bossBar.setTitle(titleTranslation.left(), titleTranslation.right());
        bossBar.setColor(BossEvent.BossBarColor.values()[colorIndex]);
        bossBar.setProgress(timer / (float) (durationTicks));
    }

    @Override
    public void run(RunningTask task) {
        if (paused) {
            if (!wasPaused) {
                wasPaused = true;
                updateBossBar();
            }

            return;
        }

        wasPaused = false;

        if (timer-- == 0) {
            task.cancel();
            return;
        }

        if (timer % 20 == 0) {
            eachSecond();
        }
    }

    private void eachSecond() {
        updateBossBar();

        if (!alertSound) return;

        int remaining = timer / 20;

        if (remaining > 5) return;

        for (ServerPlayer player : bossBar.getPlayers()) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.BLOCKS, 2f, 1f);
        }
    }

    public void addPlayers(Iterable<? extends ServerPlayer> players) {
        synchronized (this) {
            if (bossBar != null) {
                bossBar.addPlayers(players);
                return;
            }

            if (this.players == null) {
                this.players = new ArrayList<>();
            }

            for (ServerPlayer player : players) {
                this.players.add(player);
            }
        }
    }

    public static Builder builder(Translations translations, Object subject) {
        return new Builder(translations, subject);
    }

    public static class Builder {
        private final Translations translations;
        private final Object subject;
        private ResourceLocation identifier;
        private boolean cycleColor = false, alertSound = false;
        private int durationTicks = 600;
        private BossEvent.BossBarColor color = BossEvent.BossBarColor.GREEN;

        private Builder(Translations translations, Object subject) {
            this.translations = translations;
            this.subject = subject;
        }

        public Builder withIdentifier(ResourceLocation identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withCycleColor(boolean cycleColor) {
            this.cycleColor = cycleColor;
            return this;
        }

        public Builder withAlertSound(boolean alertSound) {
            this.alertSound = alertSound;
            return this;
        }

        public Builder withDurationTicks(int durationTicks) {
            this.durationTicks = durationTicks;
            return this;
        }

        public Builder withColor(BossEvent.BossBarColor color) {
            this.color = color;
            return this;
        }

        public BossBarTimer build() {
            ResourceLocation id = identifier;

            if (identifier == null) {
                String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789/._-";
                id = ResourceLocation.fromNamespaceAndPath("mgl_bbt", StringUtil.getRandomString(alphabet, 16, new Random()));
            }

            return new BossBarTimer(translations, id, subject, cycleColor, alertSound, durationTicks, color);
        }
    }
}
