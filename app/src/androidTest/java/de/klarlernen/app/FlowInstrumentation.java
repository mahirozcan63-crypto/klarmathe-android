package de.klarlernen.app;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Native UI smoke test; uses only an isolated emulator with synthetic learning data. */
public class FlowInstrumentation extends Instrumentation {
    MainActivity activity;
    interface Checked { void run() throws Exception; }
    void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    void ui(Checked task) throws Exception {
        Throwable[] error={null};
        runOnMainSync(()->{try{task.run();}catch(Throwable e){error[0]=e;}});
        waitForIdleSync();
        if(error[0]!=null)throw new Exception("UI task failed",error[0]);
    }
    List<View> views(View root){List<View> out=new ArrayList<>();out.add(root);if(root instanceof ViewGroup){ViewGroup group=(ViewGroup)root;for(int i=0;i<group.getChildCount();i++)out.addAll(views(group.getChildAt(i)));}return out;}
    View button(String text){for(View v:views(activity.getWindow().getDecorView()))if(v instanceof Button&&text.contentEquals(((Button)v).getText()))return v;throw new AssertionError("Button absent: "+text);}
    boolean hasButton(String text){for(View v:views(activity.getWindow().getDecorView()))if(v instanceof Button&&text.contentEquals(((Button)v).getText()))return true;return false;}
    void tap(String title)throws Exception{
        View[] target={null};Rect bounds=new Rect();
        ui(()->{target[0]=button(title);target[0].requestRectangleOnScreen(new Rect(0,0,target[0].getWidth(),target[0].getHeight()),true);});
        SystemClock.sleep(250);
        ui(()->require(target[0].isEnabled()&&target[0].getGlobalVisibleRect(bounds)&&bounds.width()>0&&bounds.height()>0,"Button clipped or disabled: "+title));
        long now=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,bounds.centerX(),bounds.centerY(),0);
        MotionEvent up=MotionEvent.obtain(now,now+50,MotionEvent.ACTION_UP,bounds.centerX(),bounds.centerY(),0);
        sendPointerSync(down);sendPointerSync(up);down.recycle();up.recycle();waitForIdleSync();SystemClock.sleep(100);
    }
    EditText answerField(){for(View v:views(activity.getWindow().getDecorView()))if(v instanceof EditText&&activity.t("answer").contentEquals(v.getContentDescription()))return(EditText)v;throw new AssertionError("Answer field absent");}
    void answer(String value)throws Exception{ui(()->answerField().setText(value));}
    void correct()throws Exception{
        JSONObject q=activity.engine.current();
        if(q.getString("type").equals("choice"))tap(q.getString("answer"));else answer(q.getString("answer"));
        tap(activity.t("check"));
        require(activity.engine.active().getJSONObject("draft").getJSONObject("feedback").getString("status").equals("correct"),"Verified answer rejected");
        tap(activity.t("next"));
    }
    void launch(){Intent intent=new Intent(getTargetContext(),MainActivity.class);intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);activity=(MainActivity)startActivitySync(intent);waitForIdleSync();SystemClock.sleep(300);require(activity.engine!=null,"Startup failed");}
    AccessibilityNodeInfo dialogButton(AccessibilityNodeInfo node,String title){if(node==null)return null;if(node.isClickable()&&title.contentEquals(node.getText()==null?"":node.getText()))return node;for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo found=dialogButton(node.getChild(i),title);if(found!=null)return found;}return null;}
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    @Override public void onStart(){Bundle result=new Bundle();try{
        getTargetContext().getSharedPreferences("learning",0).edit().clear().commit();launch();
        ui(()->{for(View v:views(activity.getWindow().getDecorView()))if(v instanceof CheckBox&&v.getTag()!=null)((CheckBox)v).setChecked(v.getTag().equals("math"));});
        tap(activity.t("save"));SystemClock.sleep(500);
        AccessibilityNodeInfo startCheck=dialogButton(getUiAutomation().getRootInActiveWindow(),activity.t("diagnostic"));
        require(startCheck!=null&&startCheck.performAction(AccessibilityNodeInfo.ACTION_CLICK),"Startcheck dialog inaccessible");waitForIdleSync();SystemClock.sleep(500);
        require(activity.engine.active().getJSONArray("queue").length()==6,"Initial check must contain six questions");
        answer("999999");tap(activity.t("check"));tap(activity.t("next"));
        for(int i=1;i<6;i++)correct();
        require(activity.summary.getInt("total")==6&&activity.summary.getInt("correct")==5,"Assessment summary mismatch");
        tap(activity.t("today"));tap(activity.t("start"));
        answer("999999");tap(activity.t("check"));
        require(!activity.engine.active().getJSONObject("draft").getBoolean("locked"),"Wrong practice answer must allow retry");
        tap(activity.t("hint")+" 0/3");answer("");tap("1");
        require(activity.engine.active().getJSONObject("draft").getString("answer").equals("1"),"Math keypad edited a stale field after redraw");
        correct();answer("3/2");ui(()->activity.finish());launch();tap(activity.t("continue"));
        require(activity.engine.active().getJSONObject("draft").getString("answer").equals("3/2"),"Draft lost after restart");
        for(int i=1;i<5;i++)correct();
        require(activity.summary.getInt("help")==1&&activity.summary.getInt("independent")==4,"Assisted progress mismatch");
        tap(activity.t("review"));JSONObject entry=activity.engine.entry();String originalId=null;
        for(int i=0;i<activity.engine.state.getJSONArray("reviews").length();i++){JSONObject r=activity.engine.state.getJSONArray("reviews").getJSONObject(i);if(r.getString("id").equals(entry.getString("reviewId")))originalId=r.getString("questionId");}
        require(originalId!=null&&!originalId.equals(activity.engine.current().getString("id")),"Review must use a different question");
        int count=activity.engine.active().getJSONArray("queue").length();for(int i=0;i<count;i++)correct();
        tap(activity.t("progress"));require(activity.route.equals("progress")&&!activity.engine.latest().isEmpty(),"Progress screen absent");
        tap(activity.t("subjects"));tap(activity.t("lesson"));
        require(getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK),"System back action failed");
        waitForIdleSync();SystemClock.sleep(400);require(activity.route.equals("today"),"Android 16 back callback did not return to Today");
        tap(activity.t("subjects"));tap(activity.t("lesson"));tap(activity.t("exam")+" · 5");
        require(!hasButton(activity.t("solution"))&&!hasButton(activity.t("hint")+" 0/3"),"Exam exposes hints");
        for(int i=0;i<5;i++)correct();
        require(activity.summary.getInt("total")==5&&activity.summary.getInt("independent")==5,"Exam summary mismatch");
        result.putString("FLOW_RESULT","PASS");result.putString("DETAILS","Profile; six-question assessment; retry; hint; keypad; restart persistence; similar review; progress; five-question exam passed");finish(0,result);
    }catch(Throwable error){result.putString("FLOW_RESULT","FAIL");result.putString("DETAILS",error.toString());android.util.Log.e("PhoneFlow","Smoke test failed",error);finish(1,result);}}
}
