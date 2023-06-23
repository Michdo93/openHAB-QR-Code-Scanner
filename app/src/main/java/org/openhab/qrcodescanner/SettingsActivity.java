package org.openhab.qrcodescanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String languageValue = sharedPreferences.getString("language_preference", "en");

        updateActionBarTitle();

        if (languageValue.equals("de")) {
            getSupportActionBar().setTitle(getString(R.string.settings_activity_title_text_de));
        } else if (languageValue.equals("en")) {
            getSupportActionBar().setTitle(getString(R.string.settings_activity_title_text_en));
        }

        // Ändere die Schriftart und Farbe des Titels
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));

        // Ändere die Farbe des zurück-Pfeils (Up-Navigation)
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ImageView settingsIcon = findViewById(R.id.settings_icon);
        settingsIcon.setOnClickListener(this::closeSettings);
    }

    private void updateActionBarTitle() {
        String languageValue = sharedPreferences.getString("language_preference", "en");

        if (languageValue.equals("de")) {
            getSupportActionBar().setTitle(getString(R.string.settings_activity_title_text_de));
        } else if (languageValue.equals("en")) {
            getSupportActionBar().setTitle(getString(R.string.settings_activity_title_text_en));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences sharedPreferences;
        private String languageValue;
        private String protocolValue;
        private String ipAddressValue;
        private String portValue;
        private String usernameValue;
        private String passwordValue;
        private PreferenceCategory languageCategory;
        private PreferenceCategory connectionCategory;
        private ListPreference languagePreference;
        private ListPreference protocolPreference;
        private EditTextPreference ipAddressPreference;
        private EditTextPreference portPreference;
        private EditTextPreference usernamePreference;
        private EditTextPreference passwordPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            updateSummary();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (isAdded() && getActivity() != null) {
                if (key.equals("language_preference")) {
                    ((SettingsActivity) getActivity()).updateActionBarTitle();
                }
                updateSummary();
            }
        }

        private void updateSummary() {
            languageValue = sharedPreferences.getString("language_preference", "");

            languageCategory = findPreference("language_category");
            connectionCategory = findPreference("connection_category");

            if (languageValue.equals("de")) {
                languageCategory.setTitle(getString(R.string.language_category_de));
                connectionCategory.setTitle(getString(R.string.connection_category_de));
            } else if (languageValue.equals("en")) {
                languageCategory.setTitle(getString(R.string.language_category_en));
                connectionCategory.setTitle(getString(R.string.connection_category_en));
            }

            languagePreference = findPreference("language_preference");
            if (languagePreference != null) {
                languageValue = sharedPreferences.getString("language_preference", "");
                if (languageValue != null && languageValue.equals("de")) {

                    languagePreference.setTitle(getString(R.string.language_preference_title_de));
                    languagePreference.setDialogTitle(getString(R.string.language_preference_summary_de));
                    languagePreference.setSummary(getString(R.string.language_preference_summary_de));
                    languagePreference.setEntries(R.array.language_entries_de);
                } else if (languageValue != null && languageValue.equals("en")) {
                    languagePreference.setTitle(getString(R.string.language_preference_title_en));
                    languagePreference.setDialogTitle(getString(R.string.language_preference_summary_en));
                    languagePreference.setSummary(getString(R.string.language_preference_summary_en));
                    languagePreference.setEntries(R.array.language_entries_en);
                }
                languagePreference.setEntryValues(R.array.language_values);
            }

            protocolPreference = findPreference("protocol_preference");
            if (protocolPreference != null) {
                protocolValue = sharedPreferences.getString("protocol_preference", "");
                if (languageValue != null && languageValue.equals("de")) {
                    protocolPreference.setTitle(getString(R.string.protocol_preference_title_de));
                    protocolPreference.setDialogTitle(getString(R.string.protocol_preference_summary_de));
                    protocolPreference.setSummary(getString(R.string.protocol_preference_summary_de));
                } else if (languageValue != null && languageValue.equals("en")) {
                    protocolPreference.setTitle(getString(R.string.protocol_preference_title_en));
                    protocolPreference.setDialogTitle(getString(R.string.protocol_preference_summary_en));
                    protocolPreference.setSummary(getString(R.string.protocol_preference_summary_en));
                }
                protocolPreference.setEntries(R.array.protocol_entries);
                protocolPreference.setEntryValues(R.array.protocol_values);
            }

            ipAddressPreference = findPreference("ip_address_preference");
            if (ipAddressPreference != null) {
                ipAddressValue = sharedPreferences.getString("ip_address_preference", "");
                if (languageValue.equals("de")) {
                    ipAddressPreference.setTitle(getString(R.string.ip_address_preference_title_de));
                    ipAddressPreference.setSummary(getString(R.string.ip_address_preference_summary_de));
                    ipAddressPreference.setDialogTitle(getString(R.string.ip_address_preference_dialog_title_de));
                } else if (languageValue.equals("en")) {
                    ipAddressPreference.setTitle(getString(R.string.ip_address_preference_title_en));
                    ipAddressPreference.setSummary(getString(R.string.ip_address_preference_summary_en));
                    ipAddressPreference.setDialogTitle(getString(R.string.ip_address_preference_dialog_title_en));
                }
                ipAddressPreference.setDefaultValue(getString(R.string.default_ip_address));
            }

            portPreference = findPreference("port_preference");
            if (portPreference != null) {
                portValue = sharedPreferences.getString("port_preference", "");
                if (languageValue.equals("de")) {
                    portPreference.setTitle(getString(R.string.port_preference_title_de));
                    portPreference.setSummary(getString(R.string.port_preference_summary_de));
                    portPreference.setDialogTitle(getString(R.string.port_preference_dialog_title_de));
                } else if (languageValue.equals("en")) {
                    portPreference.setTitle(getString(R.string.port_preference_title_en));
                    portPreference.setSummary(getString(R.string.port_preference_summary_en));
                    portPreference.setDialogTitle(getString(R.string.port_preference_dialog_title_en));
                }
                portPreference.setDefaultValue(getString(R.string.default_port));
            }

            usernamePreference = findPreference("username_preference");
            if (usernamePreference != null) {
                usernameValue = sharedPreferences.getString("username_preference", "");
                if (languageValue.equals("de")) {
                    usernamePreference.setTitle(getString(R.string.username_preference_title_de));
                    usernamePreference.setSummary(getString(R.string.username_preference_summary_de));
                    usernamePreference.setDialogTitle(getString(R.string.username_preference_dialog_title_de));
                } else if (languageValue.equals("en")) {
                    usernamePreference.setTitle(getString(R.string.username_preference_title_en));
                    usernamePreference.setSummary(getString(R.string.username_preference_summary_en));
                    usernamePreference.setDialogTitle(getString(R.string.username_preference_dialog_title_en));
                }
                usernamePreference.setDefaultValue(getString(R.string.default_username));
            }

            passwordPreference = findPreference("password_preference");
            if (passwordPreference != null) {
                passwordValue = sharedPreferences.getString("password_preference", "");
                if (languageValue.equals("de")) {
                    passwordPreference.setTitle(getString(R.string.password_preference_title_de));
                    passwordPreference.setSummary(getString(R.string.password_preference_summary_de));
                    passwordPreference.setDialogTitle(getString(R.string.password_preference_dialog_title_de));
                } else if (languageValue.equals("en")) {
                    passwordPreference.setTitle(getString(R.string.password_preference_title_en));
                    passwordPreference.setSummary(getString(R.string.password_preference_summary_en));
                    passwordPreference.setDialogTitle(getString(R.string.password_preference_dialog_title_en));
                }
                passwordPreference.setDefaultValue(getString(R.string.default_password));
            }
        }
    }

    public void closeSettings(View view) {
        finish();
    }
}
