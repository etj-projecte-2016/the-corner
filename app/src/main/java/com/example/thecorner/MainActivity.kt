package com.example.thecorner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import com.example.thecorner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController


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
}
