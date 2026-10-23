package com.group4.fruitshop.ui.admin.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.group4.fruitshop.R;
import com.group4.fruitshop.TestData;
import com.group4.fruitshop.data.DatabaseHelper;
import com.group4.fruitshop.data.dao.OrderDao;
import com.group4.fruitshop.util.OrderStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

/**
 * Admin order management (TV1) on the seed orders:
 * #4 An today Pending 60,000 · #3 Binh Pending 180,000 · #2 An Confirmed 120,000 · #1 An Completed 130,000.
 */
@RunWith(RobolectricTestRunner.class)
public class OrderManagementActivityTest {

    private Context context;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstance();
        context = ApplicationProvider.getApplicationContext();
        TestData.logInAs(context, "admin", "admin123");
    }

    @After
    public void tearDown() {
        DatabaseHelper.resetInstance();
    }

    @Test
    public void showsEveryOrderNewestFirst_withCustomerTotalAndAction() {
        try (ActivityScenario<OrderManagementActivity> scenario =
                     ActivityScenario.launch(OrderManagementActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView list = activity.findViewById(R.id.rvOrders);
                assertEquals(4, list.getAdapter().getItemCount());

                View first = itemAt(list, 0);
                assertEquals("Order #4", text(first, R.id.tvOrderId));
                assertEquals("Nguyen Van An · 0901234567", text(first, R.id.tvOrderCustomer));
                assertEquals("60,000 VND", text(first, R.id.tvOrderTotal));
                assertEquals("Pending", text(first, R.id.tvOrderStatus));
                assertEquals("Confirm order", text(first, R.id.btnAdvanceStatus));

                View confirmed = itemAt(list, 2);
                assertEquals("Order #2", text(confirmed, R.id.tvOrderId));
                assertEquals("Mark completed", text(confirmed, R.id.btnAdvanceStatus));

                View completed = itemAt(list, 3);
                assertEquals(View.GONE, completed.findViewById(R.id.btnAdvanceStatus).getVisibility());

                assertEquals("4 orders · Total 490,000 VND", text(activity.getWindow().getDecorView(),
                        R.id.tvOrderSummary));
                assertEquals("All (4)", text(activity.getWindow().getDecorView(), R.id.chipOrderAll));
                assertEquals("Pending (2)", text(activity.getWindow().getDecorView(), R.id.chipOrderPending));
            });
        }
    }

    @Test
    public void statusChips_filterTheList() {
        try (ActivityScenario<OrderManagementActivity> scenario =
                     ActivityScenario.launch(OrderManagementActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView list = activity.findViewById(R.id.rvOrders);

                activity.findViewById(R.id.chipOrderCompleted).performClick();
                assertEquals(1, list.getAdapter().getItemCount());
                assertEquals("Order #1", text(itemAt(list, 0), R.id.tvOrderId));

                activity.findViewById(R.id.chipOrderPending).performClick();
                assertEquals(2, list.getAdapter().getItemCount());

                activity.findViewById(R.id.chipOrderAll).performClick();
                assertEquals(4, list.getAdapter().getItemCount());
            });
        }
    }

    @Test
    public void actionButton_movesOrderPendingToConfirmedToCompleted() {
        try (ActivityScenario<OrderManagementActivity> scenario =
                     ActivityScenario.launch(OrderManagementActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView list = activity.findViewById(R.id.rvOrders);
                OrderDao orderDao = new OrderDao(activity);

                itemAt(list, 0).findViewById(R.id.btnAdvanceStatus).performClick();
                confirmLatestDialog();
                assertEquals(OrderStatus.CONFIRMED, orderDao.getById(4).getStatus());
                assertEquals("Order #4 is now Confirmed", ShadowToast.getTextOfLatestToast());
                assertEquals("Confirmed", text(itemAt(list, 0), R.id.tvOrderStatus));
                assertEquals("Pending (1)", text(activity.getWindow().getDecorView(), R.id.chipOrderPending));

                itemAt(list, 0).findViewById(R.id.btnAdvanceStatus).performClick();
                confirmLatestDialog();
                assertEquals(OrderStatus.COMPLETED, orderDao.getById(4).getStatus());
                assertEquals(View.GONE, itemAt(list, 0).findViewById(R.id.btnAdvanceStatus).getVisibility());
                assertEquals("Completed (2)",
                        text(activity.getWindow().getDecorView(), R.id.chipOrderCompleted));
            });
        }
    }

    @Test
    public void cancellingTheDialog_keepsTheStatus() {
        try (ActivityScenario<OrderManagementActivity> scenario =
                     ActivityScenario.launch(OrderManagementActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView list = activity.findViewById(R.id.rvOrders);
                itemAt(list, 0).findViewById(R.id.btnAdvanceStatus).performClick();

                AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
                assertNotNull(dialog);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
                shadowOf(Looper.getMainLooper()).idle();

                assertEquals(OrderStatus.PENDING, new OrderDao(activity).getById(4).getStatus());
            });
        }
    }

    private static void confirmLatestDialog() {
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertNotNull("a confirmation dialog must be shown", dialog);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
    }

    /** Lays the list out so the ViewHolder at {@code position} exists, then returns its view. */
    private static View itemAt(RecyclerView list, int position) {
        list.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(4000, View.MeasureSpec.EXACTLY));
        list.layout(0, 0, 1080, 4000);
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        assertNotNull("no item at position " + position, holder);
        return holder.itemView;
    }

    private static String text(View root, int id) {
        return ((TextView) root.findViewById(id)).getText().toString();
    }
}
