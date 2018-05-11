package brett.lednavigation;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
/**This test will fail without a hue bridge present*/
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoadLightsAndGroupsUITest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void loadLightsAndGroupsUITest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {//wait for bridge discovery
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataInteraction linearLayout = onData(anything())
                                               .inAdapterView(allOf(withId(R.id.bridge_discovery_result_list),
                                                       childAtPosition(
                                                               withClassName(is("android.support.constraint.ConstraintLayout")),
                                                               7)))
                                               .atPosition(0);
        linearLayout.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try { //leave time to press link button on bridge
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.support.design.widget.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0)),
                        1),
                        isDisplayed()));
        navigationMenuItemView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction switch_ = onView(
                allOf(withId(R.id.onSwitch), withText("On"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        4),
                                1),
                        isDisplayed()));
        switch_.perform(click());

        ViewInteraction switch_2 = onView(
                allOf(withId(R.id.onSwitch), withText("Off"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        4),
                                1),
                        isDisplayed()));
        switch_2.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.colorButton), withText("Color Shape"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        4),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        pressBack();

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.support.design.widget.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction navigationMenuItemView2 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0)),
                        2),
                        isDisplayed()));
        navigationMenuItemView2.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.flContent),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        DataInteraction appCompatCheckedTextView = onData(anything())
                                                           .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                   childAtPosition(
                                                                           withId(R.id.contentPanel),
                                                                           0)))
                                                           .atPosition(0);
        appCompatCheckedTextView.perform(click());

        DataInteraction appCompatCheckedTextView2 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(1);
        appCompatCheckedTextView2.perform(click());

        DataInteraction appCompatCheckedTextView3 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(2);
        appCompatCheckedTextView3.perform(click());

        DataInteraction appCompatCheckedTextView4 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(3);
        appCompatCheckedTextView4.perform(click());

        DataInteraction appCompatCheckedTextView5 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(1);
        appCompatCheckedTextView5.perform(click());

        DataInteraction appCompatCheckedTextView6 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(3);
        appCompatCheckedTextView6.perform(click());

        DataInteraction appCompatCheckedTextView7 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(0);
        appCompatCheckedTextView7.perform(click());

        DataInteraction appCompatCheckedTextView8 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(2);
        appCompatCheckedTextView8.perform(click());

        DataInteraction appCompatCheckedTextView9 = onData(anything())
                                                            .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                    childAtPosition(
                                                                            withId(R.id.contentPanel),
                                                                            0)))
                                                            .atPosition(3);
        appCompatCheckedTextView9.perform(click());

        DataInteraction appCompatCheckedTextView10 = onData(anything())
                                                             .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                     childAtPosition(
                                                                             withId(R.id.contentPanel),
                                                                             0)))
                                                             .atPosition(2);
        appCompatCheckedTextView10.perform(click());

        DataInteraction appCompatCheckedTextView11 = onData(anything())
                                                             .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                     childAtPosition(
                                                                             withId(R.id.contentPanel),
                                                                             0)))
                                                             .atPosition(1);
        appCompatCheckedTextView11.perform(click());

        DataInteraction appCompatCheckedTextView12 = onData(anything())
                                                             .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                                                                     childAtPosition(
                                                                             withId(R.id.contentPanel),
                                                                             0)))
                                                             .atPosition(0);
        appCompatCheckedTextView12.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction switch_3 = onView(
                allOf(withId(R.id.onSwitch), withText("Off"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        1),
                                2),
                        isDisplayed()));
        switch_3.perform(click());

        ViewInteraction switch_4 = onView(
                allOf(withId(R.id.onSwitch), withText("On"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        1),
                                2),
                        isDisplayed()));
        switch_4.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.colorButton), withText("Color Shape"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        1),
                                3),
                        isDisplayed()));
        appCompatButton3.perform(click());

        pressBack();

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                               && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
