package com.example.apicar

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var refresh: ProgressBar
    private lateinit var apiService: ApiService
    private lateinit var originalList: List<Car>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        val editTextName = findViewById<EditText>(R.id.name)
        val editTextPrice = findViewById<EditText>(R.id.price)
        val editTextImageUrl = findViewById<EditText>(R.id.image)
        val fullOptionCheck = findViewById<CheckBox>(R.id.full)
        val buttonAddCar = findViewById<Button>(R.id.ADD)
        refresh = findViewById(R.id.refrech)
        listView = findViewById(R.id.lv)
        val searchText = findViewById<EditText>(R.id.searh)

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apiyes.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)

        // Load initial data
        getData()

        // Set up the button click listener to add a car
        buttonAddCar.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val priceStr = editTextPrice.text.toString().trim()
            val image = editTextImageUrl.text.toString().trim()
            val isFullOptions = fullOptionCheck.isChecked

            if (name.isEmpty() || priceStr.isEmpty() || image.isEmpty()) {
                Toast.makeText(this, "Please add the car information", Toast.LENGTH_SHORT).show()
            } else {
                val price = priceStr.toDoubleOrNull()

                if (price != null) {
                    val car = Car(
                        id = 0,
                        name = name,
                        price = price,
                        isFullOptions = isFullOptions,
                        image = image
                    )

                    refresh.visibility = View.VISIBLE

                    apiService.addCar(car).enqueue(object : Callback<AddResponse> {
                        override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                            refresh.visibility = View.GONE

                            if (response.isSuccessful) {
                                val addResponse = response.body()
                                if (addResponse != null) {
                                    Toast.makeText(applicationContext, addResponse.status_message, Toast.LENGTH_LONG).show()
                                    if (addResponse.status == 1) {
                                        getData()
                                    }
                                }
                            } else {
                                Toast.makeText(applicationContext, "Failed to add Car", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                            refresh.visibility = View.GONE
                            Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up item click listener for the ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedCar = originalList[position]

            // Start UpdateActivity with the selected car's details
            val intent = Intent(this, MainActivity2::class.java).apply {
                putExtra("id", selectedCar.id)
                putExtra("name", selectedCar.name)
                putExtra("prix", selectedCar.price)
                putExtra("check", selectedCar.isFullOptions)
                putExtra("image", selectedCar.image)
            }
            startActivity(intent)
        }
    }

    private fun getData() {
        refresh.visibility = View.VISIBLE
        val call = apiService.getCars()
        call.enqueue(object : Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                refresh.visibility = View.GONE
                if (response.isSuccessful) {
                    val cars = response.body() ?: emptyList()
                    originalList = cars

                    // Create a list of car names with prices
                    val carNames = cars.map { "${it.name} - ${it.price} MAD" }

                    adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, carNames)
                    listView.adapter = adapter
                } else {
                    Toast.makeText(this@MainActivity, "Error retrieving data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                refresh.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { car ->
                car.name.contains(query, ignoreCase = true)
            }
        }

        val carNames = filteredList.map { "${it.name} - ${it.price} MAD" }
        adapter.clear()
        adapter.addAll(carNames)
        adapter.notifyDataSetChanged()
    }
}
