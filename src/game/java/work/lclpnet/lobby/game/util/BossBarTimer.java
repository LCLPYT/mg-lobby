package work.lclpnet.lobby.game.util;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.SchedulerAction;
import work.lclpnet.kibu.scheduler.api.TaskHandle;
import work.lclpnet.kibu.scheduler.api.TaskScheduler;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.kibu.translate.bossbar.BossBarProvider;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class BossBarTimer implements SchedulerAction {

    private final TranslationService translationService;
    private final Identifier id;
    private final Object subject;
    private final boolean cycleColor;
    private final boolean alertSound;
    private final int durationTicks;
    private final List<Runnable> whenDone = new ArrayList<>();
    private volatile List<ServerPlayerEntity> players = null;
    private volatile TranslatedBossBar bossBar;
    private TaskHandle taskHandle;
    private boolean paused = false;
    private boolean wasPaused = false;
    private int colorIndex;
    private int timer;

    private BossBarTimer(TranslationService translationService, Identifier id, Object subject, boolean cycleColor,
                         boolean alertSound, int durationTicks, BossBar.Color color) {
        this.translationService = translationService;
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

            bossBar = translationService.translateBossBar(id, translation.left(), translation.right())
                    .with(bossBarProvider).formatted(Formatting.YELLOW);

            bossBar.setColor(BossBar.Color.values()[colorIndex]);
            bossBar.setPercent(1f);

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
                    styled(subject, Formatting.AQUA, Formatting.BOLD)
            });
        }

        int seconds = timer / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return Pair.of("lobby.countdown.title.minutes", new Object[] {
                    styled(subject, Formatting.AQUA, Formatting.BOLD),
                    minutes,
                    seconds
            });
        }

        return Pair.of("lobby.countdown.title.seconds", new Object[] {
                styled(subject, Formatting.AQUA, Formatting.BOLD),
                seconds
        });
    }

    private void updateBossBar() {
        if (cycleColor) {
            colorIndex = (colorIndex + 1) % BossBar.Color.values().length;
        }

        var titleTranslation = titleTranslation();

        bossBar.setTitle(titleTranslation.left(), titleTranslation.right());
        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.setPercent(timer / (float) (durationTicks));
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

        for (ServerPlayerEntity player : bossBar.getPlayers()) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.BLOCKS, 2f, 1f);
        }
    }

    public void addPlayers(Iterable<? extends ServerPlayerEntity> players) {
        synchronized (this) {
            if (bossBar != null) {
                bossBar.addPlayers(players);
                return;
            }

            if (this.players == null) {
                this.players = new ArrayList<>();
            }

            for (ServerPlayerEntity player : players) {
                this.players.add(player);
            }
        }
    }

    public static Builder builder(TranslationService translationService, Object subject) {
        return new Builder(translationService, subject);
    }

    public static class Builder {
        private final TranslationService translationService;
        private final Object subject;
        private Identifier identifier;
        private boolean cycleColor = false, alertSound = false;
        private int durationTicks = 600;
        private BossBar.Color color = BossBar.Color.GREEN;

        private Builder(TranslationService translationService, Object subject) {
            this.translationService = translationService;
            this.subject = subject;
        }

        public Builder withIdentifier(Identifier identifier) {
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

        public Builder withColor(BossBar.Color color) {
            this.color = color;
            return this;
        }

        public BossBarTimer build() {
            Identifier id = identifier;

            if (identifier == null) {
                String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789/._-";
                id = new Identifier("mgl_bbt", StringUtil.getRandomString(alphabet, 16, new Random()));
            }

            return new BossBarTimer(translationService, id, subject, cycleColor, alertSound, durationTicks, color);
        }
    }
}
