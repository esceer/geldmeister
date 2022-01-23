package com.esceer.geldmeister

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

const val PREFERENCES_ID = "com.esceer.geldmeister.EXPENSES"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadExpenseHistory()
        setupOnClickListeners()
    }

    private fun loadExpenseHistory() {
        val sharedPref = getSharedPreferences(PREFERENCES_ID, MODE_PRIVATE)
        findViewById<EditText>(R.id.text_box_balance_bill).apply {
            val billExpenseType = getString(R.string.label_bill)
            setText(formatWithThousandSeparator(sharedPref.getInt(billExpenseType, 0)))
        }
        findViewById<EditText>(R.id.text_box_balance_auto).apply {
            val autoExpenseType = getString(R.string.label_auto)
            setText(formatWithThousandSeparator(sharedPref.getInt(autoExpenseType, 0)))
        }
        findViewById<EditText>(R.id.text_box_balance_misc).apply {
            val miscExpenseType = getString(R.string.label_misc)
            setText(formatWithThousandSeparator(sharedPref.getInt(miscExpenseType, 0)))
        }
    }

    private fun setupOnClickListeners() {
        findViewById<Button>(R.id.button_submit).setOnClickListener {
            onSubmitExpense()
        }

        findViewById<Button>(R.id.button_reset).setOnClickListener {
            onResetAll()
        }
    }

    private fun onResetAll() {
        findViewById<ChipGroup>(R.id.chip_group_expense_type).children.forEach {
            val expenseType = (it as Chip).text.toString()
            resetExpense(expenseType)
        }
        reinitialize()
    }

    private fun onSubmitExpense() {
        val expenseType = findExpenseType()
        if (expenseType != null) {
            val amount = findExpenseAmount()
            if (amount != 0) {
                addExpense(expenseType, amount)
                reinitialize()
            }
        } else {
            val errorMsg = getString(R.string.error_msg_missing_expense_type)
            Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun findExpenseType(): String? {
        val expenseTypeChipGroup = findViewById<ChipGroup>(R.id.chip_group_expense_type)
        val expenseTypeChipId = expenseTypeChipGroup.checkedChipId
        val expenseTypeChip = findViewById<Chip>(expenseTypeChipId)
        return expenseTypeChip?.text?.toString()
    }

    private fun findExpenseAmount(): Int {
        val expenseEditText = findViewById<EditText>(R.id.text_box_expense)
        val text = expenseEditText.text.takeUnless { it.isEmpty() }

        if (text == null) {
            expenseEditText.error = getString(R.string.warn_msg_zero_amount)
            return 0
        }
        return text.toString().toInt()
    }

    private fun addExpense(type: String, amount: Int) {
        val sharedPref = getSharedPreferences(PREFERENCES_ID, MODE_PRIVATE)
        val balance = sharedPref.getInt(type, 0)
        saveExactExpenseAmount(type, balance + amount)
    }

    private fun resetExpense(type: String) {
        saveExactExpenseAmount(type, 0)
    }

    private fun saveExactExpenseAmount(type: String, newAmount: Int) {
        val sharedPref = getSharedPreferences(PREFERENCES_ID, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(type, newAmount)
            apply()
        }
    }

    private fun reinitialize() {
        finish()
        startActivity(intent)
    }

    private companion object {
        private fun formatWithThousandSeparator(number: Int) = String.format("%,d", number)
    }
}