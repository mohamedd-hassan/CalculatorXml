package com.mohamed.calculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mohamed.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentExpression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, navigationBars.top, navigationBars.right, navigationBars.bottom)
            insets
        }
        addDigitCallBack()
        addOperatorCallBacks()
    }

    private fun addDigitCallBack(){
        binding.buttonNine.setOnClickListener(::onClickNumber)
        binding.buttonEight.setOnClickListener(::onClickNumber)
        binding.buttonSeven.setOnClickListener(::onClickNumber)
        binding.buttonSix.setOnClickListener(::onClickNumber)
        binding.buttonFive.setOnClickListener(::onClickNumber)
        binding.buttonFour.setOnClickListener(::onClickNumber)
        binding.buttonThree.setOnClickListener(::onClickNumber)
        binding.buttonTwo.setOnClickListener(::onClickNumber)
        binding.buttonOne.setOnClickListener(::onClickNumber)
        binding.buttonZero.setOnClickListener(::onClickNumber)
    }

    private fun addOperatorCallBacks(){
        binding.buttonClear.setOnClickListener {
            clear()
        }

        binding.buttonBackspace.setOnClickListener {
            backSpace()
        }

        binding.buttonPercent.setOnClickListener {
            onClickPercent()
        }

        binding.buttonDivide.setOnClickListener(::appendOperator)
        binding.buttonTimes.setOnClickListener(::appendOperator)
        binding.buttonPlus.setOnClickListener(::appendOperator)
        binding.buttonMinus.setOnClickListener(::appendOperator)


        binding.buttonEqual.setOnClickListener {
            calculateResult()
        }

        binding.buttonDot.setOnClickListener {
            onClickDot()
        }

        binding.buttonSign.setOnClickListener {

        }
    }

    private fun appendOperator(v: View){
        val operator = (v as Button).text.toString()

        val standardOperator = if (operator == "x") "*" else operator

        if (currentExpression.isNotEmpty() && isLastCharDigit()){
            val currentText = "$currentExpression $operator "
            currentExpression += " $standardOperator "
            binding.textCurrent.text = currentText
        } else {
            Toast.makeText(this, "Invalid Format", LENGTH_SHORT).show()
        }
    }

    private fun onClickPercent(){
        if (currentExpression.isEmpty() || !isLastCharDigit()) return

        val lastNumber = getLastNumber()
        if (lastNumber.isNotEmpty() && !lastNumber.contains("%")) {
            currentExpression += "%"
            binding.textCurrent.text = currentExpression
        }
    }

    private fun onClickDot(){
        if (currentExpression.isEmpty() || !isLastCharDigit()) {
            currentExpression += "0."
        } else {
            val lastNumber = getLastNumber()
            if (!lastNumber.contains(".")) {
                currentExpression += "."
            }
        }
        binding.textCurrent.text = currentExpression
    }

    private fun clear(){
        currentExpression = ""
        binding.textCurrent.text = ""
        binding.textHistory.text = ""
    }

    private fun backSpace(){
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)
            binding.textCurrent.text = currentExpression
        }
    }

    private fun onClickNumber(v: View){
        val newDigit = (v as Button).text.toString()
        currentExpression += newDigit
        binding.textCurrent.text = currentExpression
    }

    private fun isLastCharDigit(): Boolean{
        if (currentExpression.isEmpty()) return false
        return currentExpression.last() in "0123456789%"
    }

    private fun getLastNumber(): String {
        val operators = "+-*/%"
        var i = currentExpression.length - 1
        while (i >= 0 && currentExpression[i] !in operators) {
            i--
        }
        return currentExpression.substring(i + 1)
    }

    private fun calculateResult(){
        if (currentExpression.isEmpty()) return

        try {
            val tokens = tokenize(currentExpression)
            val rpn = infixToRPN(tokens)
            val result = evaluateRPN(rpn)

            binding.textHistory.text = currentExpression
            currentExpression = if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }
            binding.textCurrent.text = currentExpression
        } catch (e: Exception) {
            Log.d("calc", e.stackTraceToString())
        }
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentToken = ""

        for (char in expression) {
            when {
                char.isDigit() || char == '.' || char == '%' -> {
                    currentToken += char
                }
                char in "+-*/" -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken)
                        currentToken = ""
                    }
                    tokens.add(char.toString())
                }
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken)
        }

        return tokens
    }

    private fun infixToRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = mutableListOf<String>()

        val precedence = mapOf(
            "+" to 1,
            "-" to 1,
            "*" to 2,
            "/" to 2
        )

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> {
                    output.add(token)
                }
                token in precedence -> {
                    while (operators.isNotEmpty() &&
                        operators.last() in precedence &&
                        precedence[operators.last()]!! >= precedence[token]!!) {
                        output.add(operators.removeAt(operators.size - 1))
                    }
                    operators.add(token)
                }
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.removeAt(operators.size - 1))
        }

        return output
    }

    private fun evaluateRPN(rpn: List<String>): Double {
        val stack = mutableListOf<Double>()

        for (token in rpn) {
            when {
                token.toDoubleOrNull() != null -> {
                    stack.add(token.toDouble())
                }
                token.endsWith("%") -> {
                    val numberPart = token.dropLast(1)
                    val percentValue = numberPart.toDouble() / 100.0
                    stack.add(percentValue)
                }
                token in "+-*/" -> {
                    if (stack.size < 2) {
                        Toast.makeText(this, "Invalid Format", LENGTH_SHORT).show()
                        throw IllegalArgumentException("Invalid expression")
                    }

                    val b = stack.removeAt(stack.size - 1)
                    val a = stack.removeAt(stack.size - 1)

                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> {
                            if (b == 0.0) {
                                Toast.makeText(this, "Can't Divide By Zero", LENGTH_SHORT).show()
                                throw ArithmeticException("Division by zero")
                            }
                            a / b
                        }
                        else -> {
                            Toast.makeText(this, "Unknown operator: $token", LENGTH_SHORT).show()
                            throw IllegalArgumentException("Unknown operator: $token")
                        }
                    }

                    stack.add(result)
                }
            }
        }

        if (stack.size != 1) {
            Toast.makeText(this, "Invalid Format", LENGTH_SHORT).show()
            throw IllegalArgumentException("Invalid expression")
        }
        return stack[0]
    }
}