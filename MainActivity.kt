package com.example.bit242713_foodmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var rvDishes: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var btnFilter: Button
    private lateinit var btnLang: Button
    private lateinit var fabAddDish: FloatingActionButton

    private lateinit var dishAdapter: DishAdapter
    private lateinit var repository: FoodRepository

    private var allDishes: List<Dish> = emptyList()
    private var categoryList: List<DishCategory> = emptyList()

    // Biến lưu trạng thái bộ lọc
    private var currentSearchQuery: String = ""
    private var selectedCategoryId: Int? = null
    private var isAvailableOnly: Boolean = false
    private var minPrice: Double? = null
    private var maxPrice: Double? = null
    private var currentSortOption: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ánh xạ View
        rvDishes = findViewById(R.id.rvDishes)
        edtSearch = findViewById(R.id.edtSearch)
        btnFilter = findViewById(R.id.btnFilter)
        btnLang = findViewById(R.id.btnLang)
        fabAddDish = findViewById(R.id.fabAddDish)

        // 2. Khởi tạo Database và Repository
        val db = AppDatabase.getDatabase(this)
        repository = FoodRepository(db)

        // 3. Sự kiện đổi ngôn ngữ (Đa ngôn ngữ)
        btnLang.setOnClickListener {
            val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            if (currentLang == "vi") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            } else {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi"))
            }
        }

        // 4. Cài đặt Adapter & Sự kiện Sửa/Xóa đa ngôn ngữ
        rvDishes.layoutManager = LinearLayoutManager(this)
        dishAdapter = DishAdapter(emptyList()) { selectedDish ->
            // Sử dụng getString() để lấy từ điển đa ngôn ngữ
            val options = arrayOf(getString(R.string.edit_option), getString(R.string.delete_option))

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.options_title))
                .setItems(options) { _, which ->
                    if (which == 0) {
                        val intent = Intent(this, EditDishActivity::class.java)
                        intent.putExtra("EXTRA_DISH_ID", selectedDish.id)
                        startActivity(intent)
                    } else if (which == 1) {
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.confirm_delete_title))
                            .setMessage("${getString(R.string.confirm_delete_msg)}\n\n[ ${selectedDish.name} ]")
                            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    repository.deleteDish(selectedDish)
                                    loadData()
                                }
                            }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show()
                    }
                }
                .show()
        }
        rvDishes.adapter = dishAdapter

        // 5. Bắt sự kiện Tìm kiếm theo tên
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString().trim()
                applyFilterAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 6. Bắt sự kiện bấm nút Lọc
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        // 7. Nút FAB Thêm món ăn mới
        fabAddDish.setOnClickListener {
            val intent = Intent(this, EditDishActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfEmpty()
            categoryList = repository.getAllCategories()
            allDishes = repository.getAllDishes()

            withContext(Dispatchers.Main) {
                applyFilterAndSort()
            }
        }
    }

    private fun applyFilterAndSort() {
        var filteredList = allDishes.toList()

        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.name.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        selectedCategoryId?.let { catId ->
            filteredList = filteredList.filter { it.categoryId == catId }
        }

        if (isAvailableOnly) {
            filteredList = filteredList.filter { it.isAvailable }
        }

        minPrice?.let { min ->
            filteredList = filteredList.filter { it.price >= min }
        }
        maxPrice?.let { max ->
            filteredList = filteredList.filter { it.price <= max }
        }

        filteredList = when (currentSortOption) {
            1 -> filteredList.sortedBy { it.price }
            2 -> filteredList.sortedByDescending { it.price }
            3 -> filteredList.sortedBy { it.preparationTime }
            else -> filteredList
        }

        dishAdapter.updateData(filteredList)
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)

        val rgSort = dialogView.findViewById<RadioGroup>(R.id.rgSort)
        val rbNone = dialogView.findViewById<RadioButton>(R.id.rbNone)
        val rbPriceAsc = dialogView.findViewById<RadioButton>(R.id.rbPriceAsc)
        val rbPriceDesc = dialogView.findViewById<RadioButton>(R.id.rbPriceDesc)
        val rbTimeAsc = dialogView.findViewById<RadioButton>(R.id.rbTimeAsc)

        val spinnerFilterCategory = dialogView.findViewById<Spinner>(R.id.spinnerFilterCategory)
        val cbAvailableOnly = dialogView.findViewById<CheckBox>(R.id.cbAvailableOnly)
        val edtMinPrice = dialogView.findViewById<EditText>(R.id.edtMinPrice)
        val edtMaxPrice = dialogView.findViewById<EditText>(R.id.edtMaxPrice)
        val btnApplyFilter = dialogView.findViewById<Button>(R.id.btnApplyFilter)

        // Tải danh mục vào Spinner
        val catNames = mutableListOf("--- Tất cả / All ---")
        catNames.addAll(categoryList.map { it.name })
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)
        spinnerFilterCategory.adapter = catAdapter

        // Phục hồi trạng thái bộ lọc
        when (currentSortOption) {
            1 -> rbPriceAsc.isChecked = true
            2 -> rbPriceDesc.isChecked = true
            3 -> rbTimeAsc.isChecked = true
            else -> rbNone.isChecked = true
        }

        selectedCategoryId?.let { catId ->
            val index = categoryList.indexOfFirst { it.id == catId }
            if (index >= 0) spinnerFilterCategory.setSelection(index + 1)
        }

        cbAvailableOnly.isChecked = isAvailableOnly
        minPrice?.let { edtMinPrice.setText(it.toString()) }
        maxPrice?.let { edtMaxPrice.setText(it.toString()) }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnApplyFilter.setOnClickListener {
            currentSortOption = when (rgSort.checkedRadioButtonId) {
                R.id.rbPriceAsc -> 1
                R.id.rbPriceDesc -> 2
                R.id.rbTimeAsc -> 3
                else -> 0
            }

            val selPos = spinnerFilterCategory.selectedItemPosition
            selectedCategoryId = if (selPos > 0) categoryList[selPos - 1].id else null

            isAvailableOnly = cbAvailableOnly.isChecked
            minPrice = edtMinPrice.text.toString().toDoubleOrNull()
            maxPrice = edtMaxPrice.text.toString().toDoubleOrNull()

            applyFilterAndSort()
            dialog.dismiss()
        }

        dialog.show()
    }
}