package de.klarlernen.app;

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LearningEngineTest {
    private JSONObject catalog() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/content.json")) {
            if (in == null) throw new IOException("Missing catalog");
            return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
    private LearningEngine fresh() throws Exception { return new LearningEngine(catalog(), null); }
    private void solve(LearningEngine e) throws Exception {
        e.active().getJSONObject("draft").put("answer", e.current().getString("answer"));
        assertEquals("correct", e.submit().getString("status"));
    }
    @Test public void allAuthoredAnswersAndMathStepsAreAccepted() throws Exception {
        LearningEngine e = fresh(); int count=0;
        for (int i=0; i<e.units().length(); i++) {
            JSONArray items=e.units().getJSONObject(i).getJSONArray("items");
            for (int j=0; j<items.length(); j++) {
                JSONObject q=items.getJSONObject(j); StringBuilder steps=new StringBuilder();
                JSONArray sample=q.optJSONArray("steps");
                if (sample!=null) for(int k=0;k<sample.length();k++) steps.append(sample.getString(k)).append("\n");
                assertEquals(q.getString("id"),"correct",e.check(q,q.getString("answer"),steps.toString()).getString("status")); count++;
            }
        }
        assertEquals(134,count);
    }
    @Test public void equivalentWaysAndFractionsPass() {
        assertEquals("correct",SafeMath.check("2x+4=10","3","x+2=5\nx=3").status);
        assertEquals("correct",SafeMath.check("2x+4=10","3","2x=6\nx=3").status);
        assertEquals("correct",SafeMath.check("0,5x=0.75","3/2","").status);
        assertEquals("correct",SafeMath.check("-2(x-3)=12","-3","").status);
        assertEquals("correct",SafeMath.check("x/2=0","0","").status);
        assertEquals("correct",SafeMath.check("2x+4=10","3","2x\u22126=0\nx=3").status);
    }
    @Test public void invalidStepIsNotOverriddenByCorrectResult() {
        assertEquals("incorrect",SafeMath.check("2x+4=10","3","2x=10").status);
        assertEquals("incorrect",SafeMath.check("2x+4=10","3","0=0").status);
    }
    @Test public void unsupportedInputIsNotRecordedAsWrong() throws Exception {
        LearningEngine e=fresh();e.begin("practice","math",1,false);
        e.active().getJSONObject("draft").put("answer","eval(1)");
        assertEquals("unsupported",e.submit().getString("status"));
        assertEquals(0,e.state.getJSONArray("attempts").length());
        assertEquals(0,e.state.getJSONArray("reviews").length());
    }
    @Test public void retryCorrectnessCountsAsAssistedAndCreatesReview() throws Exception {
        LearningEngine e=fresh(); e.begin("practice","math",1,false);
        e.active().getJSONObject("draft").put("answer","999999");
        assertEquals("incorrect",e.submit().getString("status"));
        assertFalse(e.active().getJSONObject("draft").getBoolean("locked"));
        solve(e);
        assertEquals(2,e.state.getJSONArray("attempts").length());
        assertTrue(e.latest().get(0).getBoolean("help"));
        assertEquals(1,e.state.getJSONArray("reviews").length());
    }
    @Test public void hintAssistanceAndDuplicateSubmitAreSeparate() throws Exception {
        LearningEngine e=fresh();e.begin("practice","math",1,false);
        e.active().getJSONObject("draft").put("hints",1);solve(e);e.submit();
        assertEquals(1,e.state.getJSONArray("attempts").length());
        assertTrue(e.latest().get(0).getBoolean("help"));
    }
    @Test public void savedDraftSurvivesRestart() throws Exception {
        LearningEngine e=fresh();e.begin("practice","math",5,false);
        JSONObject d=e.active().getJSONObject("draft");d.put("answer","-3/2").put("steps","2x=-3").put("hints",2);
        LearningEngine restored=new LearningEngine(e.catalog,e.state.toString());
        assertEquals("-3/2",restored.active().getJSONObject("draft").getString("answer"));
        assertEquals("2x=-3",restored.active().getJSONObject("draft").getString("steps"));
        assertEquals(2,restored.active().getJSONObject("draft").getInt("hints"));
    }
    @Test public void reviewChoosesADifferentQuestionForSameObjective() throws Exception {
        LearningEngine e=fresh();e.begin("practice","math",1,false);
        String before=e.current().getString("id"),objective=e.current().getString("objective");
        e.skip();e.begin("review","math",1,false);
        assertNotEquals(before,e.current().getString("id"));assertEquals(objective,e.current().getString("objective"));
    }
    @Test public void examFinishesAndSkippedQuestionsRemainInTotal() throws Exception {
        LearningEngine e=fresh();e.begin("exam","math",5,false);
        for(int i=0;i<4;i++) { e.active().getJSONObject("draft").put("answer","999999");e.submit();e.next(); }
        JSONObject summary=e.skip();assertNotNull(summary);
        assertEquals(5,summary.getInt("total"));assertEquals(1,summary.getInt("skipped"));assertNull(e.active());
    }
    @Test public void threeIndependentAnswersIncreaseDifficulty() throws Exception {
        LearningEngine e=fresh();
        for(int i=0;i<3;i++){e.begin("practice","math",1,false);solve(e);e.next();}
        assertEquals(2,e.state.getJSONObject("levels").getInt("math"));
    }
    @Test public void invalidBackupIsRejected() throws Exception {
        LearningEngine e=fresh();e.begin("practice","math",1,false);
        JSONObject invalid=new JSONObject(e.state.toString());invalid.getJSONObject("active").put("index",99);
        try { e.validate(invalid);fail("Invalid index accepted"); } catch (JSONException expected) {}
    }
}
