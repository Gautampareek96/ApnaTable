package com.example.khaugali

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreateTableActivity : AppCompatActivity() {

    private lateinit var tableGrid: GridLayout
    private lateinit var addTableBtn: FloatingActionButton
    private lateinit var toolLayout: LinearLayout
    private var isToolLayoutVisible = false
    private lateinit var toolTable: ImageButton
    private lateinit var toolChair: ImageButton
    private lateinit var toolErase: ImageButton
    private lateinit var toolEdit: ImageButton
    private lateinit var arrowUp: ImageButton
    private lateinit var arrowDown: ImageButton
    private lateinit var arrowLeft: ImageButton
    private lateinit var arrowRight: ImageButton
    private lateinit var moveArrowLayout: View
    private var isMoveMode = false

    private val selectedCells = mutableListOf<TextView>()
    private var selectedTool = ToolType.TABLE
    private var tableCount = 0
    private var chairCount = 0

    private val totalCols = 9
    private val totalRows = 18

    enum class ToolType {
        TABLE, CHAIR, ERASE, EDIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_table)

        // Grid and tools
        tableGrid = findViewById(R.id.tableGrid)
        addTableBtn = findViewById(R.id.addTableBtn)
        toolLayout = findViewById(R.id.toolLayout)

        toolTable = findViewById(R.id.toolTable)
        toolChair = findViewById(R.id.toolChair)
        toolErase = findViewById(R.id.toolDelete)
        toolEdit = findViewById(R.id.toolEdit)
        arrowUp = findViewById(R.id.arrowUp)
        arrowDown = findViewById(R.id.arrowDown)
        arrowLeft = findViewById(R.id.arrowLeft)
        arrowRight = findViewById(R.id.arrowRight)
        moveArrowLayout = findViewById(R.id.moveArrowLayout)

        arrowUp.setOnClickListener { moveSelectedBy(0, -1) }
        arrowDown.setOnClickListener { moveSelectedBy(0, 1) }
        arrowLeft.setOnClickListener { moveSelectedBy(-1, 0) }
        arrowRight.setOnClickListener { moveSelectedBy(1, 0) }

        val moveBtn = findViewById<ImageButton>(R.id.moveBtn)

        moveBtn.setOnClickListener {
            if (selectedCells.isEmpty()) {
                Toast.makeText(this, "Select cells first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isMoveMode = !isMoveMode

            if (isMoveMode) {
                moveArrowLayout.visibility = View.VISIBLE
                moveBtn.setBackgroundColor(Color.LTGRAY) // visually shows it's active
            } else {
                moveArrowLayout.visibility = View.GONE
                moveBtn.setBackgroundColor(Color.TRANSPARENT)
            }
        }



        // (+) Toggle button
        addTableBtn.setOnClickListener {
            isToolLayoutVisible = !isToolLayoutVisible
            toolLayout.visibility = if (isToolLayoutVisible) View.VISIBLE else View.GONE
        }

        // Tool selection
        toolTable.setOnClickListener {
            selectedTool = ToolType.TABLE
            highlightSelectedTool()
        }

        toolChair.setOnClickListener {
            selectedTool = ToolType.CHAIR
            highlightSelectedTool()
        }

        toolErase.setOnClickListener {
            selectedTool = ToolType.ERASE
            highlightSelectedTool()
        }

        toolEdit.setOnClickListener {
            selectedTool = ToolType.EDIT
            highlightSelectedTool()
        }

        findViewById<ImageButton>(R.id.rotateBtn).setOnClickListener {
            rotateSelectedItems()
        }

        findViewById<ImageButton>(R.id.combineBtn).setOnClickListener {
            combineSelectedItems()
        }

        findViewById<ImageButton>(R.id.renameBtn).setOnClickListener {
            renameCombinedItem()
        }

        tableGrid.post {
            populateEmptyGrid()
        }
    }
    private fun moveSelectedBy(dx: Int, dy: Int) {
        if (selectedCells.isEmpty()) return

        val positions = selectedCells.map { tableGrid.indexOfChild(it) }
        val rowColList = positions.map { it / totalCols to it % totalCols }

        val newPositions = mutableListOf<Pair<Int, Int>>()


        // Check if move is possible
        for ((row, col) in rowColList) {
            val newRow = row + dy
            val newCol = col + dx

            if (newRow !in 0 until totalRows || newCol !in 0 until totalCols) {
                Toast.makeText(this, "Move out of bounds", Toast.LENGTH_SHORT).show()
                return
            }

            val newIndex = newRow * totalCols + newCol
            val newCell = tableGrid.getChildAt(newIndex) as TextView

            if (newCell.tag != null && !selectedCells.contains(newCell)) {
                Toast.makeText(this, "Cannot move: overlaps with another item", Toast.LENGTH_SHORT).show()
                return
            }

            newPositions.add(newRow to newCol)
        }

        // Store original content
        val originalData = selectedCells.map {
            Triple(it.text.toString(), it.tag, (it.background as? android.graphics.drawable.ColorDrawable)?.color ?: Color.WHITE)
        }

        // Clear current cells
        for (cell in selectedCells) {
            cell.text = ""
            cell.tag = null
            cell.setBackgroundColor(Color.WHITE)
        }

        // Apply to new positions
        selectedCells.clear()
        for (i in newPositions.indices) {
            val (newRow, newCol) = newPositions[i]
            val index = newRow * totalCols + newCol
            val cell = tableGrid.getChildAt(index) as TextView
            val (text, tag, color) = originalData[i]

            cell.text = text
            cell.tag = tag
            cell.setBackgroundColor(color)
            selectedCells.add(cell)
        }
    }


    private fun highlightSelectedTool() {
        val selectedColor = Color.parseColor("#FFD580")
        val normalColor = Color.parseColor("#E9A557")

        findViewById<LinearLayout>(R.id.editActionsLayout).visibility =
            if (selectedTool == ToolType.EDIT) View.VISIBLE else View.GONE

        toolTable.setBackgroundColor(if (selectedTool == ToolType.TABLE) selectedColor else normalColor)
        toolChair.setBackgroundColor(if (selectedTool == ToolType.CHAIR) selectedColor else normalColor)
        toolErase.setBackgroundColor(if (selectedTool == ToolType.ERASE) selectedColor else normalColor)
        toolEdit.setBackgroundColor(if (selectedTool == ToolType.EDIT) selectedColor else normalColor)

        if (selectedTool != ToolType.EDIT) {
            clearSelectedCells()
        }
    }

    private fun populateEmptyGrid() {
        tableGrid.removeAllViews()
        tableGrid.columnCount = totalCols
        tableGrid.rowCount = totalRows

        val displayMetrics = resources.displayMetrics
        val cellWidth = displayMetrics.widthPixels / totalCols
        val cellHeight = displayMetrics.heightPixels / totalRows

        for (row in 0 until totalRows) {
            for (col in 0 until totalCols) {
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(row, GridLayout.FILL),
                    GridLayout.spec(col, GridLayout.FILL)
                ).apply {
                    width = cellWidth
                    height = cellHeight
                    setMargins(1, 1, 1, 1)
                }

                val cell = TextView(this).apply {
                    layoutParams = params
                    setBackgroundColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    tag = null
                    setOnClickListener { handleCellClick(this) }
                }

                tableGrid.addView(cell)
            }
        }
    }

    private fun handleCellClick(cell: TextView) {
        when (selectedTool) {
            ToolType.TABLE -> {
                if (cell.tag == null) {
                    cell.text = "T${++tableCount}"
                    cell.setBackgroundColor(Color.parseColor("#E9A557"))
                    cell.setTextColor(Color.WHITE)
                    cell.tag = "TABLE"
                }
            }

            ToolType.CHAIR -> {
                if (cell.tag == null) {
                    cell.text = "C${++chairCount}"
                    cell.setBackgroundColor(Color.parseColor("#6AB04A"))
                    cell.setTextColor(Color.WHITE)
                    cell.tag = "CHAIR"
                }
            }

            ToolType.ERASE -> {
                cell.text = ""
                cell.setBackgroundColor(Color.WHITE)
                cell.tag = null
                selectedCells.remove(cell)
            }

            ToolType.EDIT -> {
                if (cell.tag != null) {
                    if (selectedCells.contains(cell)) {
                        selectedCells.remove(cell)
                        // Restore original color
                        val color = when (cell.tag) {
                            "TABLE" -> "#E9A557"
                            "CHAIR" -> "#6AB04A"
                            else -> "#FFFFFF"
                        }
                        cell.setBackgroundColor(Color.parseColor(color))
                    } else {
                        selectedCells.add(cell)
                        cell.setBackgroundColor(Color.parseColor("#FFDD57")) // Yellow highlight
                    }
                }
            }
        }
    }

//    private fun moveSelectedItems() {
//        for (cell in selectedCells) {
//            cell.setBackgroundColor(Color.LTGRAY)
//            cell.text = "Moved"
//        }
//        clearSelectedCells()
//    }

    private fun rotateSelectedItems() {
        if (selectedCells.size <= 1) return

        val positions = selectedCells.map { tableGrid.indexOfChild(it) }
        val rows = positions.map { it / totalCols }
        val cols = positions.map { it % totalCols }

        val minRow = rows.minOrNull()!!
        val maxRow = rows.maxOrNull()!!
        val minCol = cols.minOrNull()!!
        val maxCol = cols.maxOrNull()!!

        val numRows = maxRow - minRow + 1
        val numCols = maxCol - minCol + 1

        // Validate rectangular selection
        for (row in minRow..maxRow) {
            for (col in minCol..maxCol) {
                val index = row * totalCols + col
                val cell = tableGrid.getChildAt(index) as TextView
                if (!selectedCells.contains(cell)) {
                    Toast.makeText(this, "Rotation allowed only for rectangular selections", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        // Step 1: Save original data (text and tag only)
        val originalCells = Array(numRows) { row ->
            Array(numCols) { col ->
                val index = (minRow + row) * totalCols + (minCol + col)
                val cell = tableGrid.getChildAt(index) as TextView
                Pair(cell.text.toString(), cell.tag)
            }
        }

        // Step 2: Prepare new rotated positions
        val newPositions = mutableListOf<Pair<TextView, Pair<String, Any?>>>()
        var canRotate = true

        loop@ for (col in 0 until numCols) {
            for (row in numRows - 1 downTo 0) {
                val newRow = minRow + col
                val newCol = minCol + (numRows - 1 - row)

                if (newRow >= totalRows || newCol >= totalCols) {
                    Toast.makeText(this, "Cannot rotate: item would go out of bounds", Toast.LENGTH_SHORT).show()
                    canRotate = false
                    break@loop
                }

                val index = newRow * totalCols + newCol
                val cell = tableGrid.getChildAt(index) as TextView

                if (cell.tag != null && !selectedCells.contains(cell)) {
                    Toast.makeText(this, "Cannot rotate: overlaps with another item", Toast.LENGTH_SHORT).show()
                    canRotate = false
                    break@loop
                }

                val data = originalCells[row][col]
                newPositions.add(cell to data)
            }
        }

        if (!canRotate || newPositions.size != selectedCells.size) return

        // Step 3: Clear original cells
        for (cell in selectedCells) {
            cell.text = ""
            cell.tag = null
            cell.setBackgroundColor(Color.WHITE)
        }

        // Step 4: Apply text and tag to new rotated cells
        for ((cell, data) in newPositions) {
            cell.text = data.first
            cell.tag = data.second
            if (data.second == "TABLE") {
                cell.setBackgroundColor(Color.parseColor("#A76F00"))
            } else if (data.second == "CHAIR") {
                cell.setBackgroundColor(Color.parseColor("#996633"))
            }
        }

        selectedCells.clear()
        selectedCells.addAll(newPositions.map { it.first })
    }

    private fun combineSelectedItems() {
        if (selectedCells.isEmpty()) return

        val name = "T${++tableCount}"
        for (cell in selectedCells) {
            cell.text = name
            cell.setBackgroundColor(Color.parseColor("#A76F00"))
            cell.tag = "TABLE"
        }
        clearSelectedCells()
    }

    private fun renameCombinedItem() {
        if (selectedCells.isEmpty()) return

        val name = "T${++tableCount}"
        for (cell in selectedCells) {
            cell.text = name
        }
    }

    private fun clearSelectedCells() {
        for (cell in selectedCells) {
            val originalColor = when (cell.tag) {
                "TABLE" -> "#E9A557"
                "CHAIR" -> "#6AB04A"
                else -> "#FFFFFF"
            }
            cell.setBackgroundColor(Color.parseColor(originalColor))
        }
        selectedCells.clear()
    }
}
