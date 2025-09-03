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
            v.setPadding(
                navigationBars.left, navigationBars.top, navigationBars.right, navigationBars.bottom
            )
            insets
        }
        addDigitCallBack()
        addOperatorCallBacks()
    }

    private fun addDigitCallBack() {
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

    private fun addOperatorCallBacks() {
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
            onClickSign()
        }
    }

    private fun clear() {
        currentExpression = ""
        binding.textCurrent.text = ""
        binding.textHistory.text = ""
    }

    private fun backSpace() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)
            binding.textCurrent.text = currentExpression
        }
    }

    private fun appendOperator(v: View) {
        val operator = (v as Button).text.toString()

        if (currentExpression.isEmpty() && operator == "-") {
            currentExpression += operator
            binding.textCurrent.text = currentExpression
            return
        }

        if (operator == "-" && currentExpression.isNotEmpty() && currentExpression.trimEnd()
                .last() in "+-/x"
        ) {
            currentExpression += operator
            binding.textCurrent.text = currentExpression
            return
        }

        if (currentExpression.isNotEmpty() && isLastCharDigit()) {
            currentExpression += " $operator "
            binding.textCurrent.text = currentExpression
        } else {
            Toast.makeText(this, getString(R.string.invalid_format), LENGTH_SHORT).show()
        }
    }

    private fun onClickPercent() {
        if (currentExpression.isEmpty() || !isLastCharDigit()) return

        val lastNumber = getLastNumber()
        if (lastNumber.isNotEmpty() && !lastNumber.contains("%")) {
            currentExpression += "%"
            binding.textCurrent.text = currentExpression
        } else {
            Toast.makeText(this, getString(R.string.invalid_format), LENGTH_SHORT).show()
        }
    }

    private fun onClickSign() {
        if (currentExpression.isEmpty()) {
            currentExpression = "-"
            binding.textCurrent.text = currentExpression
            return
        }

        val trimmed = currentExpression.trimEnd()
        if (trimmed.isEmpty()) return

        var numberEnd = trimmed.length

        if (trimmed.last() == '%') {
            numberEnd--
        }

        var i = numberEnd - 1
        while (i >= 0 && (trimmed[i].isDigit() || trimmed[i] == '.')) {
            i--
        }

        val hasNegativeSign = i >= 0 && trimmed[i] == '-' &&
                (i == 0 || trimmed.take(i).trimEnd().last() in "+-x/")

        val numberStart = if (hasNegativeSign) {
            i
        } else {
            i + 1
        }

        if (numberStart >= numberEnd) return

        val beforeNumber = currentExpression.substring(0, numberStart)
        val number = trimmed.substring(numberStart, numberEnd)
        val afterNumber = currentExpression.substring(numberEnd)

        val newNumber = if (hasNegativeSign) {
            number.substring(1)
        } else {
            "-$number"
        }

        currentExpression = beforeNumber + newNumber + afterNumber
        binding.textCurrent.text = currentExpression
    }

    private fun onClickDot() {
        if (currentExpression.isEmpty()) {
            currentExpression += "0."
        } else if (currentExpression.last() == '-' && (currentExpression.length == 1 || currentExpression[currentExpression.length - 2] in "+-*/")) {
            currentExpression += "0."
        } else if (isLastCharDigit()) {
            val lastNumber = getLastNumber()
            if (!lastNumber.contains(".")) {
                currentExpression += "."
            }
        } else {
            currentExpression += "0."
        }
        binding.textCurrent.text = currentExpression
    }


    private fun onClickNumber(v: View) {
        val newDigit = (v as Button).text.toString()
        if (currentExpression.isNotEmpty()) {
            if (currentExpression.last() == '%') {
                Toast.makeText(this, getString(R.string.invalid_format), LENGTH_SHORT).show()
                return
            }
        }
        currentExpression += newDigit
        binding.textCurrent.text = currentExpression
    }


    private fun isLastCharDigit(): Boolean {
        if (currentExpression.isEmpty()) return false
        return currentExpression.last() in "0123456789%"
    }

    private fun getLastNumber(): String {
        val trimmed = currentExpression.trimEnd()
        if (trimmed.isEmpty()) return ""

        val operators = "+-*/"
        var i = trimmed.length - 1

        if (i >= 0 && trimmed[i] == '%') {
            i--
        }

        while (i >= 0 && trimmed[i] !in operators) {
            i--
        }

        if (i >= 0 && trimmed[i] == '-') {
            val beforeMinus = trimmed.take(i).trimEnd()
            if (i == 0 || beforeMinus.isEmpty() || beforeMinus.last() in operators) {
                i--
            }
        }

        return trimmed.substring(i + 1)
    }

    private fun calculateResult() {
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

        val cleanExpression = expression.replace(" ", "")

        for (i in cleanExpression.indices) {
            val char = cleanExpression[i]
            when {
                char.isDigit() || char == '.' || char == '%' -> {
                    currentToken += char
                }

                char == '-' -> {
                    val isNegativeSign = i == 0 || cleanExpression[i - 1] in "+-*/x"

                    if (isNegativeSign) {
                        currentToken += char
                    } else {
                        if (currentToken.isNotEmpty()) {
                            tokens.add(currentToken)
                            currentToken = ""
                        }
                        tokens.add(char.toString())
                    }
                }

                char in "+*/" || char == 'x' -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken)
                        currentToken = ""
                    }
                    tokens.add(if (char == 'x') "*" else char.toString())
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
            "+" to 1, "-" to 1, "*" to 2, "/" to 2
        )

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null || token.endsWith("%") -> {
                    output.add(token)
                }

                token in precedence -> {
                    while (operators.isNotEmpty() && operators.last() in precedence && precedence[operators.last()]!! >= precedence[token]!!) {
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
                token.endsWith("%") -> {
                    val numberPart = token.dropLast(1)
                    val percentValue = numberPart.toDouble() / 100.0
                    stack.add(percentValue)
                }

                token.toDoubleOrNull() != null -> {
                    stack.add(token.toDouble())
                }

                token in "+-*/" -> {
                    if (stack.size < 2) {
                        Toast.makeText(this, getString(R.string.invalid_format), LENGTH_SHORT)
                            .show()
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
                                Toast.makeText(
                                    this, getString(R.string.divide_by_zero_error), LENGTH_SHORT
                                ).show()
                                throw ArithmeticException("Division by zero")
                            }
                            a / b
                        }

                        else -> {
                            Toast.makeText(
                                this, getString(R.string.unknown_operator, token), LENGTH_SHORT
                            ).show()
                            throw IllegalArgumentException("Unknown operator: $token")
                        }
                    }

                    stack.add(result)
                }
            }
        }

        if (stack.size != 1) {
            Toast.makeText(this, getString(R.string.invalid_format), LENGTH_SHORT).show()
            throw IllegalArgumentException("Invalid expression")
        }
        return stack[0]
    }
}