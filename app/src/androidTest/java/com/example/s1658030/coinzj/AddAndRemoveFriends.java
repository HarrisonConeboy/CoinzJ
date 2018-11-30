package com.example.s1658030.coinzj;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
public class AddAndRemoveFriends {

    @Rule
    public ActivityTestRule<SignIn> mActivityTestRule = new ActivityTestRule<>(SignIn.class);


    @Test
    public void addAndRemoveFriends() {

        //Firstly we create a new account named add@friend.com
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction emailField = onView(
                allOf(withId(R.id.emailField),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        emailField.perform(replaceText("add@friend.com"), closeSoftKeyboard());


        //We now set their password to be an awful 12345678
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction passwordField = onView(
                allOf(withId(R.id.passwordField),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        passwordField.perform(replaceText("12345678"), closeSoftKeyboard());


        //Now a simple click on the Sign Up button
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction signUpButton = onView(
                allOf(withId(R.id.signUpButton), withText("Sign Up"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        signUpButton.perform(click());


        //Next from the Main Menu, we press on Friends to take us to the Friends Activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction friendsButton = onView(
                allOf(withId(R.id.friendsButton), withText("Friends"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        friendsButton.perform(click());


        //Now we enter a friend's email being "new@friend.com" into the EditText
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction friendUsername = onView(
                allOf(withId(R.id.friendUsername),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        friendUsername.perform(replaceText("new@friend.com"), closeSoftKeyboard());


        //Now we press the button Add Friend, which should result in it being displayed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction addFriend = onView(
                allOf(withId(R.id.addFriend), withText("Add Friend"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        addFriend.perform(click());


        //We now attempt to add ourselves, first by typing our email into the EditText
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        friendUsername.perform(replaceText("add@friend.com"), closeSoftKeyboard());


        //Next a simple press on the Add Friend button, which should result in a Toast alerting us
        // that we are unable top add ourselves
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addFriend.perform(click());


        //Now we will add another friend, we enter "another@friend.com" into the EditText
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        friendUsername.perform(replaceText("another@friend.com"), closeSoftKeyboard());


        //Another press on Add Friend should add and update the listView
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addFriend.perform(click());


        //Now we try to add a username which does not exist, "non@existent.com"
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        friendUsername.perform(replaceText("non@existent.com"), closeSoftKeyboard());


        //Again we press Add Friend which results in a failure to add the friend
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addFriend.perform(click());


        //Finally we will remove all of our friends added,
        // first by pressing on the top friend's name
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataInteraction appCompatTextView = onData(anything())
                .inAdapterView(allOf(withId(R.id.friends),
                        childAtPosition(
                                withId(R.id.rl2),
                                0)))
                .atPosition(0);
        appCompatTextView.perform(click());


        //Next we press the Remove Friend option in the Dialog,
        // this removes the friend from the listView
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataInteraction appCompatTextView2 = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(2);
        appCompatTextView2.perform(click());


        //Now we follow the same steps as before to remove the second friend
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataInteraction appCompatTextView3 = onData(anything())
                .inAdapterView(allOf(withId(R.id.friends),
                        childAtPosition(
                                withId(R.id.rl2),
                                0)))
                .atPosition(0);
        appCompatTextView3.perform(click());


        //Press Remove Friend on Dialog
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataInteraction appCompatTextView4 = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(2);
        appCompatTextView4.perform(click());


        //Contemplate decisions for 3 seconds
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
