package edu.weber.cs.w01113559.emojimoodtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.weber.cs.w01113559.emojimoodtracker.Settings.DeleteDataDialog;
import edu.weber.cs.w01113559.emojimoodtracker.Settings.MainSettingsFragment;
import edu.weber.cs.w01113559.emojimoodtracker.data.model.GlobalAppDatabase;
import edu.weber.cs.w01113559.emojimoodtracker.databinding.ActivityDashboardBinding;
import edu.weber.cs.w01113559.emojimoodtracker.notifications.NotificationDatabase;
import edu.weber.cs.w01113559.emojimoodtracker.notifications.NotificationUtil;

public class DashboardActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        LogoutDialog.signOutInterface {

    private FirebaseAuth auth;

    private View root;
    private ActivityDashboardBinding binding;
    private Toolbar toolbar;
    private AppBarConfiguration appBarConfiguration;

    private NavHostFragment navHostFragment;
    private NavController navController;
    private NavOptions options;
    private NavOptions.Builder optionsBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(root);
        setSupportActionBar(toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        // Create Notification channel if it doesn't already exist
        NotificationUtil.createNotificationChannel(
                getApplicationContext(),
                NotificationDatabase.getDontForgetToRecordReminderData(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.action_settings) {
            // Remove Graph Button
            setGraphFabVisibility(false);

            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.nav_host_fragment_content_main, new MainSettingsFragment())
                    .commit();
            return true;
        } else if (itemID == R.id.action_sign_out) {
            DialogFragment logoutDialog = new LogoutDialog(this);
            logoutDialog.show(getSupportFragmentManager(), "logoutDialog");
            return true;
        } else if (itemID == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Handles when the user clicks a {@link Preference}.
     * @param caller {@link PreferenceFragmentCompat} the fragment that called this.
     * @param pref {@link Preference} the preference that was clicked to trigger this.
     * @return {@link Boolean} true: handled. false: unable to handle.
     */
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        switch (pref.getKey()) {
            case "scheduleNotification":
                //ToDo: Handle Schedlue Notification Click
                return true;
            case "removeNotification":
                // ToDo: handle Remove Notification Click
                return true;
            case "clearData":
                DialogFragment clearDataDialog = new DeleteDataDialog();
                clearDataDialog.show(getSupportFragmentManager(), "clearDataDialog");
                return true;
        }
        return false;
    }

    /**
     * Initialize Activity Data
     */
    private void initData(){

        // Firebase
        auth = FirebaseAuth.getInstance();
        if (GlobalAppDatabase.getAppDatabaseInstance() == null) {
            GlobalAppDatabase.initializeAppDatabaseInstance(getApplicationContext());
        }

        // View Elements
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        root = binding.getRoot();
        toolbar = binding.toolbar;
        toolbar.setTitle("Record Mood");

        // Navigation
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        optionsBuilder = new NavOptions.Builder();
        options = optionsBuilder
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build();
        binding.graphFAB.setOnClickListener(view -> {
            setGraphFabVisibility(false);
            navController.navigate(R.id.graphFragment, null, options);  // Navigate to Graph Page
            binding.toolbar.setTitle("Mood Summary Graph");


        });
    }

    /**
     * Sets the {@link com.google.android.material.floatingactionbutton.FloatingActionButton} for the graph page as visible or not.
     * @param visibility {@link Boolean} true- visible, false- hidden.
     */
    private void setGraphFabVisibility(@NonNull Boolean visibility) {
        if (visibility) {
            binding.graphFAB.show();
        } else {
            binding.graphFAB.hide();
        }
    }

    @Override
    public Boolean signout() {
        if (auth != null && auth.getCurrentUser() != null) {
            auth = FirebaseAuth.getInstance();
            auth.signOut();
            Snackbar.make(root, "Successfully Logged Out.", Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(root.getContext(), LoginActivity.class));
            finish();
            return true;
        } else {
            Snackbar.make(root, "There was an error signing out.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }
}