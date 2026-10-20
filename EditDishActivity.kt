package com.example.bit242713_foodmanager

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditDishActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var edtName: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtPrepTime: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var edtImageUrl: EditText
    private lateinit var edtDescription: EditText
    private lateinit var switchAvailable: SwitchCompat
    private lateinit var btnSave: Button

    private lateinit var repository: FoodRepository
    private var categoryList: List<DishCategory> = emptyList()

    private var currentDishId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_dish)

        tvTitle = findViewById(R.id.tvTitle)
        edtName = findViewById(R.id.edtName)
        edtPrice = findViewById(R.id.edtPrice)
        edtPrepTime = findViewById(R.id.edtPrepTime)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        edtImageUrl = findViewById(R.id.edtImageUrl)
        edtDescription = findViewById(R.id.edtDescription)
        switchAvailable = findViewById(R.id.switchAvailable)
        btnSave = findViewById(R.id.btnSave)

        val db = AppDatabase.getDatabase(this)
        repository = FoodRepository(db)

        currentDishId = intent.getIntExtra("EXTRA_DISH_ID", -1)

        loadCategories()
        btnSave.setOnClickListener {
            saveDish()
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            categoryList = repository.getAllCategories()
            val categoryNames = categoryList.map { it.name }

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    this@EditDishActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    categoryNames
                )
                spinnerCategory.adapter = adapter

                if (currentDishId != -1) {
                    tvTitle.text = "Chỉnh sửa món ăn"
                    loadDishData(currentDishId)
                }
            }
        }
    }

    private fun loadDishData(dishId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val allDishes = repository.getAllDishes()
            val dish = allDishes.find { it.id == dishId }

            dish?.let { d ->
                withContext(Dispatchers.Main) {
                    edtName.setText(d.name)
                    edtPrice.setText(d.price.toString())
                    edtPrepTime.setText(d.preparationTime.toString())
                    edtDescription.setText(d.description)
                    switchAvailable.isChecked = d.isAvailable

                    val images = repository.jsonToImages(d.dishImageJson)
                    if (images.isNotEmpty()) {
                        edtImageUrl.setText(images[0].imageUrl)
                    }

                    val catIndex = categoryList.indexOfFirst { it.id == d.categoryId }
                    if (catIndex >= 0) {
                        spinnerCategory.setSelection(catIndex)
                    }
                }
            }
        }
    }

    private fun saveDish() {
        val name = edtName.text.toString().trim()
        val priceStr = edtPrice.text.toString().trim()
        val prepTimeStr = edtPrepTime.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val imageUrl = edtImageUrl.text.toString().trim()
        val isAvailable = switchAvailable.isChecked

        if (name.isEmpty()) {
            edtName.error = "Tên không được để trống"
            edtName.requestFocus()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            edtPrice.error = "Giá phải lớn hơn 0"
            edtPrice.requestFocus()
            return
        }

        val prepTime = prepTimeStr.toIntOrNull()
        if (prepTime == null || prepTime <= 0) {
            edtPrepTime.error = "Thời gian phải lớn hơn 0"
            edtPrepTime.requestFocus()
            return
        }

        val selectedCategoryIndex = spinnerCategory.selectedItemPosition
        if (categoryList.isEmpty() || selectedCategoryIndex < 0) {
            Toast.makeText(this, "Vui lòng chọn loại món ăn", Toast.LENGTH_SHORT).show()
            return
        }
        val categoryId = categoryList[selectedCategoryIndex].id

        val images = listOf(DishImageItem(imageUrl = imageUrl, isThumbnail = true))
        val imageJson = repository.imagesToJson(images)

        lifecycleScope.launch(Dispatchers.IO) {
            if (currentDishId == -1) {
                val newDish = Dish(
                    name = name,
                    price = price,
                    preparationTime = prepTime,
                    isAvailable = isAvailable,
                    description = description,
                    categoryId = categoryId,
                    dishImageJson = imageJson,
                    isSynced = false
                )
                repository.insertDish(newDish)
            } else {
                val updatedDish = Dish(
                    id = currentDishId,
                    name = name,
                    price = price,
                    preparationTime = prepTime,
                    isAvailable = isAvailable,
                    description = description,
                    categoryId = categoryId,
                    dishImageJson = imageJson,
                    isSynced = false
                )
                repository.updateDish(updatedDish)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditDishActivity, "Đã lưu món ăn thành công!", Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditDishActivity, "Đã lưu món ăn thành công!", Toast.LENGTH_SHORT).show()

                    // KIẾN TRÚC OFFLINE-FIRST: Kích hoạt Worker tự động đẩy lên MockAPI khi có mạng
                    val constraints = androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                    val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                        .setConstraints(constraints)
                        .build()
                    androidx.work.WorkManager.getInstance(applicationContext).enqueue(syncRequest)

                    finish()
                }
                finish()
            }
        }
    }
}