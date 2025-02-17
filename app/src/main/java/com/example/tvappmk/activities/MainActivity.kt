package com.example.tvappmk.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tvappmk.fragments.FavoritesFragment
import com.example.tvappmk.R
import com.example.tvappmk.fragments.HomeFragment
import com.example.tvappmk.model.ChannelViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var channelViewModel: ChannelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        channelViewModel = ViewModelProvider(this)[ChannelViewModel::class.java]


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_bar)

        // Set up the toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        // Set the item selected listener for BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Load HomeFragment if the "Home" item is selected
                    loadFragment(HomeFragment())
                    true
                }

                R.id.favorites -> {
                    // Load FavoritesFragment if the "Favorites" item is selected
                    loadFragment(FavoritesFragment())
                    true
                }

                else -> false
            }
        }

        // Check if the fragment is already added, to avoid replacing it on orientation change
        if (savedInstanceState == null) {
            // Initially load HomeFragment when the activity is first created
            loadFragment(HomeFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                // Handle the search functionality
                val searchView = item.actionView as SearchView
                searchView.isIconified = false
                searchView.queryHint = "Search here..."

                // Set listeners for query text changes or submission
                searchView.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        // Handle text changes (e.g., filter a RecyclerView in real-time)

                        query?.let {
                            channelViewModel.searchChannels(query)
                        }

                        Log.d("REAL-TIME-QUERY", query.toString())
                        return false
                    }
                })
                // When clicking on X, it clears the text
                searchView.setOnCloseListener {
                    true
                }
                true
            }

            // Getting settings icon and navigation to Settings Activity with Settings Fragment
            R.id.setting -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Helper function to load a fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

