package com.example.iris_kotlin_221351020

import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val modelPath = "iris.tflite"

    // Deklarasi View
    private lateinit var resultText: TextView
    private lateinit var edtSepalLength: EditText
    private lateinit var edtSepalWidth: EditText
    private lateinit var edtPetalLength: EditText
    private lateinit var edtPetalWidth: EditText
    private lateinit var checkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi view
        resultText      = findViewById(R.id.txtResult)
        edtSepalLength  = findViewById(R.id.edtSepalLengthCm)
        edtSepalWidth   = findViewById(R.id.edtSepalWidthCm)
        edtPetalLength  = findViewById(R.id.edtPetalLengthCm)
        edtPetalWidth   = findViewById(R.id.edtPetalWidthCm)
        checkButton     = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            val sepalLength = edtSepalLength.text.toString()
            val sepalWidth = edtSepalWidth.text.toString()
            val petalLength = edtPetalLength.text.toString()
            val petalWidth = edtPetalWidth.text.toString()

            if (sepalLength.isEmpty() || sepalWidth.isEmpty() || petalLength.isEmpty() || petalWidth.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = doInference(sepalLength, sepalWidth, petalLength, petalWidth)

            resultText.text = when (result) {
                0 -> "Iris Setosa"
                1 -> "Iris Versicolor"
                2 -> "Iris Virginica"
                else -> "Unknown"
            }
        }

        initInterpreter()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options().apply {
            setNumThreads(5)
            setUseNNAPI(true)
        }
        interpreter = Interpreter(loadModelFile(assets, modelPath), options)
    }

    private fun doInference(
        input1: String, input2: String,
        input3: String, input4: String
    ): Int {
        val input = arrayOf(floatArrayOf(
            input1.toFloat(),
            input2.toFloat(),
            input3.toFloat(),
            input4.toFloat()
        ))

        val output = Array(1) { FloatArray(3) }
        interpreter.run(input, output)

        Log.i("result", output[0].toList().toString())

        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }
}
