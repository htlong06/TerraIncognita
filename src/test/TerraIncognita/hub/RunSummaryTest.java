package TerraIncognita.hub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RunSummaryTest {
    @Test
    void calculateScore_returnsCorrectValue() {
        RunSummary summary = new RunSummary();
        summary.addMonsterKill();
        summary.addGold(50);
        summary.addFloorCleared();
        assertEquals(1100, summary.calculateScore());
    }

    @Test
    void addMonsterKill_incrementsCounter() {
        RunSummary summary = new RunSummary();
        summary.addMonsterKill();
        summary.addMonsterKill();
        summary.addMonsterKill();
        assertEquals(3, summary.getMonstersKilled());
    }

    @Test
    void newRunSummary_allFieldsZero() {
        RunSummary summary = new RunSummary();
        assertEquals(0, summary.getMonstersKilled());
        assertEquals(0, summary.getGoldEarned());
        assertEquals(0, summary.getExpEarned());
        assertEquals(0, summary.getFloorsCleared());
        assertEquals(0, summary.getItemsCollected());
        assertEquals(0, summary.getTimePlayed());
        assertFalse(summary.isSurvived());
    }
}
