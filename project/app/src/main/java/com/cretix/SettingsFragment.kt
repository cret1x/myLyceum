package com.cretix

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment


class FireMissilesDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.app_info))
            .setPositiveButton(getString(android.R.string.ok)
            ) { dialog, id ->

            }
        return builder.create()
    }
}


class SettingsFragment : Fragment() {

    private lateinit var settingsItems: List<SettingsItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_settings, container, false)

        settingsItems = listOf(
            SettingsItem(getString(R.string.settings_accounts), R.drawable.ic_account_box_black_24dp) {
                val intent = Intent(activity, AccountManageActivity::class.java)
                startActivity(intent)
            },
            SettingsItem(getString(R.string.settings_color), R.drawable.ic_palette_black_24dp) {
                val intent = Intent(activity, ThemeChangeActivity::class.java)
                startActivity(intent)},
            SettingsItem(getString(R.string.settings_general), R.drawable.ic_settings_black_24dp) {
                val intent = Intent(activity, MainSettingsActivity::class.java)
                startActivity(intent)
            },
            SettingsItem(getString(R.string.settings_sources), R.drawable.ic_source_black_24dp) {
                val intent = Intent(activity, SourceSelectActivity::class.java)
                startActivity(intent)
            },
            SettingsItem(getString(R.string.settings_about), R.drawable.ic_info_black_24dp) {
                val i = FireMissilesDialogFragment()
                i.show(fragmentManager!!, "a")
            }
        )
        val listView = v.findViewById<ListView>(R.id.listView)
        val listAdapter = SettingsAdapter(requireContext(), requireActivity()).apply { settingsList = settingsItems }
        listView.adapter = listAdapter
        return v
    }


}
