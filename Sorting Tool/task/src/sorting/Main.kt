package sorting

import java.io.File

enum class EnumDataType(val msgTotal: String, val msgMaxItem: String, val NewLine: String){
    NUMBER("numbers", "greatest number", " "),
    LINE("lines", "longest line", "\n"),
    WORD("words", "longest word", " "),
}

abstract class SortingTool(val vDataType: EnumDataType) {
    val arrInp = mutableListOf<String>()
    var outRez: (str: String) -> Unit = ::println
    private var pFileOut = File("none")
    abstract fun fill(pStr: String)
    abstract fun run()
    abstract fun sort()
    open fun normalize() {arrInp.sort()}

    fun openFile(nameFile: String) {
        pFileOut = File(nameFile)
        outRez = ::prn2file
    }

    private fun prn2file (string: String){
        pFileOut.appendText(string + "\n")
    }

    fun sortByCount() {
        val cnt = arrInp.size
        outRez("Total ${vDataType.msgTotal}: ${arrInp.size}.")
        val arrCntMap = mutableMapOf<String, Int>()
        for (a in arrInp.toSet()) arrCntMap[a] = arrInp.count { it == a }
        for (a in arrCntMap.values.sorted().toSet()){
            arrCntMap.filter { it.value == a }.forEach {
                outRez("${it.key}: $a time(s), ${it.value * 100 / cnt}%/n")
            }
        }
    }

    fun printRez(rez: Map<String, String>) {
        outRez("Total ${vDataType.msgTotal}: ${rez["cntAll"]}.")
        outRez("The ${vDataType.msgMaxItem}:" +
                "${vDataType.NewLine}${rez["maxElement"]}${vDataType.NewLine}" +
                "(${rez["cntMaxElement"]} time(s), ${rez["percentMexElement"]}%).")
    }
}

open class SortingToolLine(vDataType: EnumDataType = EnumDataType.LINE): SortingTool(vDataType) {

    override fun fill(pStr: String) {
        arrInp.add(pStr)
    }

    override fun run() {
        val maxArr = arrInp.maxByOrNull{ it.length }!!
        val cnt = arrInp.size
        val cntMax = arrInp.count { it == maxArr }
        printRez( mapOf(
            "cntAll" to cnt.toString(),
            "maxElement" to maxArr,
            "cntMaxElement" to cntMax.toString(),
            "percentMexElement" to (cntMax * 100 / cnt).toString()
        ))
    }

    override fun sort() {
        outRez("Total ${vDataType.msgTotal}: ${arrInp.size}.")
        outRez("Sorted data:${vDataType.NewLine}${arrInp.sorted().joinToString(vDataType.NewLine)}")
    }
}

class SortingToolWord: SortingToolLine(vDataType = EnumDataType.WORD) {
    override fun fill(pStr: String) {
        pStr.trim().split("\\s+".toRegex()).forEach { super.fill(it) }
    }
}

class SortingToolLong: SortingTool(vDataType = EnumDataType.NUMBER) {
    private val arrLong = mutableListOf<Long>()
    override fun fill(pStr: String) {

        pStr.trim().split("\\s+".toRegex()).forEach {
            if (it.matches("[+-]?\\d+".toRegex())) arrInp.add(it)
            else println("\"$it\" is not a long. It will be skipped.")
        }
    }

    override fun normalize(){
        if (arrLong.isNotEmpty()) return
        for (a in arrInp) arrLong.add(a.toLong())
        arrLong.sort()
        arrInp.clear()
        arrLong.forEach{ arrInp.add(it.toString())}
    }

    override fun run() {
        val maxArr = arrLong.maxOrNull()!!
        val cnt = arrLong.size
        val cntMax = arrLong.count { it == maxArr }
        printRez( mapOf(
            "cntAll" to cnt.toString(),
            "maxElement" to maxArr.toString(),
            "cntMaxElement" to cntMax.toString(),
            "percentMexElement" to (cntMax * 100 / cnt).toString()
        ))
    }

    override fun sort() {
        outRez("Total ${vDataType.msgTotal}: ${arrInp.size}.")
        outRez("Sorted data:${vDataType.NewLine}${arrLong.sorted().joinToString(vDataType.NewLine)}")
    }
}

fun checkArgs(args: Array<String>): MutableMap<String, String> {
    val argsList = listOf("-datatype","-sortingtype","-inputfile","-outputfile")
    val argsMap = mutableMapOf("msgResult" to "ok")
    for (i in args.indices) {
        if (args[i].lowercase() in argsList) {
            if (i != args.lastIndex) {
                if (args[i + 1][0] != '-') argsMap[args[i].lowercase()] = args[i + 1]
                else argsMap[args[i].lowercase()] = ""
            } else {
                argsMap[args[i].lowercase()] = ""
            }
        } else {
            if (args[i][0] == '-') {
                println("\"${args[i]}\" is not a valid parameter. It will be skipped.")
            }
        }
    }
    if ((argsMap["-datatype"] ?: "") == "") argsMap["-datatype"] = "word"
    else if (argsMap["-sortingtype"] == "") argsMap["-sortingtype"] = "natural"
    if (argsMap["-sortingtype"] == "") argsMap["msgResult"] = "No sorting type defined!"
    return argsMap
}

fun main(args: Array<String>) {
    val argsMap = checkArgs(args)
    if (argsMap["msgResult"] != "ok") {
        println("${argsMap["msgResult"]}")
        return
    }
    val objST = when(argsMap["-datatype"]?.lowercase()) {
        "long" -> SortingToolLong()
        "line" -> SortingToolLine()
        "word" -> SortingToolWord()
        else -> return
    }
    if ((argsMap["-inputfile"] ?: "") == "") {
        while (true){
            objST.fill(readLine() ?: break)
        }
    } else {
        val inFile = File(argsMap["-inputfile"]!! )
        for (a in inFile.readLines()) {
            objST.fill(a)
        }
    }

    if ((argsMap["-outputfile"] ?: "") != "") {
        objST.openFile(argsMap["-outputfile"]!!)
    }

    objST.normalize()
    when(argsMap["-sortingtype"]?.lowercase() ?: ""){
        "natural", "" -> objST.sort()
        "bycount" -> objST.sortByCount()
    }
}