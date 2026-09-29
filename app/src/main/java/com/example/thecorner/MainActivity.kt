package com.example.thecorner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        var onboardingResolved = false
        installSplashScreen().setKeepOnScreenCondition { !onboardingResolved }
        super.onCreate(savedInstanceState)

        // One edge-to-edge policy for the whole app. Foreground content opts in
        // to safe areas below; artwork is allowed to remain behind system bars.
        WindowCompat.enableEdgeToEdge(window)
        window.isNavigationBarContrastEnforced = false
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var safeBottomInset = 0
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { _, insets ->
            safeBottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            ).bottom
            updateNavHostBottomInset(safeBottomInset)
            insets
        }

        val navHostFragment =
            supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        lifecycleScope.launch {
            val onboardingCompleted = runCatching {
                (application as TheCornerApplication)
                    .appContainer.profileRepository.onboardingCompleted.first()
            }.getOrDefault(false)

            if (!onboardingCompleted && savedInstanceState == null) {
                navController.navigate(
                    R.id.welcomeFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.homeFragment) { inclusive = true }
                    }
                )
            }
            onboardingResolved = true
        }


        // =========================================================
        // BOTTOM NAVIGATION
        // =========================================================

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            val destinationId = item.itemId

            /*
             * Si ya estamos en el fragment raíz de esa pestaña,
             * no hacemos nada.
             */
            if (navController.currentDestination?.id == destinationId) {
                return@setOnItemSelectedListener true
            }


            /*
             * Volvemos primero al inicio del graph.
             *
             * En nuestro caso:
             *
             * homeFragment
             *
             * Esto elimina pantallas secundarias como
             * editWorkoutFragment del back stack.
             */
            navController.popBackStack(
                navController.graph.startDestinationId,
                false
            )


            /*
             * Si hemos pulsado Home, ya estamos ahí después
             * del popBackStack().
             *
             * Para cualquier otra pestaña navegamos
             * a su fragment raíz.
             */
            if (destinationId != navController.graph.startDestinationId) {

                navController.navigate(destinationId)
            }

            true
        }


        // =========================================================
        // SELECTED TAB
        // =========================================================

        navController.addOnDestinationChangedListener { _, destination, _ ->

            binding.bottomNavigation.isVisible = destination.id in setOf(
                R.id.homeFragment,
                R.id.trainingFragment,
                R.id.aiFragment,
                R.id.profileFragment,
                R.id.editWorkoutFragment
            )
            updateNavHostBottomInset(safeBottomInset)

            when (destination.id) {

                R.id.homeFragment -> {

                    binding.bottomNavigation.menu
                        .findItem(R.id.homeFragment)
                        .isChecked = true
                }


                R.id.trainingFragment,
                R.id.editWorkoutFragment -> {

                    /*
                     * Edit Workout pertenece a Training.
                     *
                     * Aunque estemos dentro de EditWorkoutFragment,
                     * mantenemos Training seleccionado en la navbar.
                     */
                    binding.bottomNavigation.menu
                        .findItem(R.id.trainingFragment)
                        .isChecked = true
                }


                R.id.aiFragment -> {

                    binding.bottomNavigation.menu
                        .findItem(R.id.aiFragment)
                        .isChecked = true
                }


                R.id.profileFragment -> {

                    binding.bottomNavigation.menu
                        .findItem(R.id.profileFragment)
                        .isChecked = true
                }
            }
        }
    }

    /**
     * BottomNavigationView owns its own system navigation area. When it is
     * hidden, normal screens receive that safe area through the nav host;
     * immersive screens protect their foreground content themselves.
     */
    private fun updateNavHostBottomInset(safeBottomInset: Int) {
        val destinationId =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)
                ?.navController
                ?.currentDestination
                ?.id
        val bottomNavigationVisible = binding.bottomNavigation.isVisible
        val cinematic = destinationId == R.id.welcomeFragment ||
            destinationId == R.id.workoutSessionFragment
        binding.navHostFragment.setPadding(
            binding.navHostFragment.paddingLeft,
            binding.navHostFragment.paddingTop,
            binding.navHostFragment.paddingRight,
            if (!bottomNavigationVisible && !cinematic) safeBottomInset else 0,
        )
    }
}
