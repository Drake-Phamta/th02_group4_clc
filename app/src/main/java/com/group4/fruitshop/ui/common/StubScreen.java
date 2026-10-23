package com.group4.fruitshop.ui.common;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.group4.fruitshop.R;

/**
 * Placeholder UI for screens that a member has not implemented yet, so the app always
 * builds and can be navigated end-to-end. Each member replaces their stub Activities;
 * once every module is merged this class is no longer used.
 */
public final class StubScreen {

    /** A button on the placeholder screen. */
    public static final class Action {
        final String label;
        final Runnable onClick;

        public Action(String label, Runnable onClick) {
            this.label = label;
            this.onClick = onClick;
        }
    }

    private StubScreen() {
    }

    public static void show(Activity activity, String screenName, String owner, Action... actions) {
        activity.setContentView(R.layout.activity_stub);
        activity.setTitle(screenName);
        ((TextView) activity.findViewById(R.id.tvStubTitle)).setText(screenName);
        ((TextView) activity.findViewById(R.id.tvStubOwner))
                .setText(activity.getString(R.string.stub_owner, owner));

        LinearLayout container = activity.findViewById(R.id.layoutStubActions);
        LayoutInflater inflater = LayoutInflater.from(activity);
        for (Action action : actions) {
            Button button = (Button) inflater.inflate(R.layout.item_stub_action, container, false);
            button.setText(action.label);
            button.setOnClickListener(v -> action.onClick.run());
            container.addView(button);
        }
    }
}
