package tasklist

import com.squareup.moshi.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.system.exitProcess
import java.io.File
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.LocalDateTime

class Task(    var year: Int,
               var month: Int,
               var dayOfMonth: Int,
               var hour: Int,
               var minute: Int,
               var priority:String, var description: List<String>){
    fun toLocalDateTime(): LocalDateTime {
        return LocalDateTime(year, month, dayOfMonth, hour, minute)
    }
}
class TaskList(){
    val tsks=ArrayDeque<Task>()
    val priorityColorMap = mapOf(
        'C' to "\u001B[101m \u001B[0m",
        'H' to "\u001B[103m \u001B[0m",
        'N' to "\u001B[102m \u001B[0m",
        'L' to "\u001B[104m \u001B[0m"
    )
    val dueColorMap = mapOf(
        'I' to "\u001B[102m \u001B[0m",
        'T' to "\u001B[103m \u001B[0m",
        'O' to "\u001B[101m \u001B[0m"
    )

    fun printTaskList(){
        if (tsks.isEmpty()) println("No tasks have been input")
        else {
            println("+----+------------+-------+---+---+--------------------------------------------+\n" +
                    "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                    "+----+------------+-------+---+---+--------------------------------------------+")
            var i = 1
            for (t in tsks) {
                val days = t.toLocalDateTime().date.daysUntil(Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date)
                val tg = when {
                    days == 0 -> "T"
                    days > 0 -> "O"
                    else -> "I"
                }
                val dueColor=dueColorMap.getOrElse(tg[0]) { tg}
                val prioColor=priorityColorMap.getOrElse(t.priority[0]) { t.priority}

                val formattedDateTime = "%04d-%02d-%02d | %02d:%02d".format(
                    t.year,
                    t.month,
                    t.dayOfMonth,
                    t.hour,
                    t.minute
                )
                val descriptionLines = mutableListOf<String>()

                for (line in t.description) {
                    if (line.length <= 44) {
                        descriptionLines.add(line)
                    } else {
                        var startIndex = 0
                        while (startIndex < line.length) {
                            val endIndex = minOf(startIndex + 44, line.length)
                            descriptionLines.add(line.substring(startIndex, endIndex))
                            startIndex = endIndex
                        }
                    }
                }

                println("| %-2d | $formattedDateTime | $prioColor | $dueColor |${descriptionLines[0].padEnd(44)}|".format(i++))
                for (i in 1 until descriptionLines.size) println("|    |            |       |   |   |${descriptionLines[i].padEnd(44)}|")
                println("+----+------------+-------+---+---+--------------------------------------------+")
            }
        }
    }
    fun readDate():LocalDate{
        var date:LocalDate= LocalDate(1,1,1)
        var dateOk=false
        do {
            println("Input the date (yyyy-mm-dd):")
            try {
                val inp=readln().trim().split("-")
                date = LocalDate(inp[0].toInt(),inp[1].toInt(),inp[2].toInt())
                dateOk=true
            }
            catch (e:IllegalArgumentException){
                println("The input date is invalid")
            }
        } while (!dateOk)
        return date
    }
    fun readTime():LocalTime{
        var time:LocalTime=LocalTime.of(1,1,1,1)
        var timeOk=false
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        do {
            println("Input the time (hh:mm):")
            try {
                val inp = readLine()!!.trim().split(":")
                time = LocalTime.of(inp[0].toInt(),inp[1].toInt())
                timeOk = true
            } catch (e: Exception) {
                println("The input time is invalid")
            }
        } while (!timeOk)
        return time
    }
    fun readTaskDescription():MutableList<String>?{
        println("Input a new task (enter a blank line to end):")
        var i=1
        var task= mutableListOf<String>()
        while (true) {
            val l = readln().trim()

            if (l == "" && i==1) {
                println("The task is blank")
            }
            if (l=="") {
                if (task.size>0) return task
                return null
            }
            task.add(l)
            i++;
        }
    }
    fun readPriority():String{
        var priority:String
        do {
            println("Input the task priority (C, H, N, L):")
            priority = readln().trim().toUpperCase()
        } while (priority !in listOf("C", "H", "N", "L"))
        return priority
    }
    fun readTask(){

        var priority=readPriority()
        var date= readDate()
        var time=readTime()
        val tskDescription=readTaskDescription()
        if (tskDescription!=null) tsks.add(Task(date.year,date.monthNumber,date.dayOfMonth,time.hour,time.minute,priority,tskDescription))

    }

    fun deleteTask(){
        printTaskList()
        if (!tsks.isEmpty())
        {
            var tskNr=readTaskNumber()
            tsks.removeAt(tskNr-1)
            println("The task is deleted")
        }
    }
    fun readTaskNumber():Int{
        var tskNrOk=false
        var tskNr=0
        do {
            println("Input the task number (1-${tsks.size}):")
            try {
                tskNr = readln().toInt()
                if (1 > tskNr || tskNr > tsks.size) throw Exception("Invalid Number")
                tskNrOk=true
            }
            catch(e:Exception){
                println("Invalid task number")
            }
        } while (!tskNrOk)
       return tskNr
    }
    fun readField():String{
        val fldOk=false
        var fld=""
        do {
            println("Input a field to edit (priority, date, time, task):")
            fld= readln().trim().lowercase()
            if (fld !in listOf("priority", "date", "time", "task")) println("Invalid field")
        } while (fld !in listOf("priority", "date", "time", "task"))
        return fld
    }
    fun editTask(){
        printTaskList()
        if(!tsks.isEmpty())
        {
            val tskNr=readTaskNumber()-1


            when (readField())
            {
                "priority"->{
                    tsks.get(tskNr).priority=readPriority()
                }
                "date"->{
                    val dt=readDate()
                    val tsk=tsks.get(tskNr)
                    tsk.year=dt.year
                    tsk.month=dt.monthNumber
                    tsk.dayOfMonth=dt.dayOfMonth
                }
                "time"->{
                    val tm=readTime()
                    val tsk=tsks.get(tskNr)
                    tsk.hour=tm.hour
                    tsk.minute=tm.minute
                }
                "task"->{
                    val tsk=readTaskDescription()
                    if (tsk!=null) tsks.get(tskNr).description=tsk.toList()
                }
            }
            print("The task is changed")
        }
    }
    fun doActions(){
        while (true) {
            println("Input an action (add, print, edit, delete, end):")
            when (readln()){
                "add" -> readTask()
                "print" -> printTaskList()
                "delete" -> deleteTask()
                "edit" -> editTask()
                "end"->{
                    println("Tasklist exiting!")
                    val jsonFile = File("tasklist.json")
                    val json = toJson()
                    jsonFile.writeText(json)
                    exitProcess(0)
                }
                else->{
                    println("The input action is invalid")
                }
            }
        }
    }

    fun toJson(): String {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(Task::class.java)
        val taskJsonList = tsks.map { adapter.toJson(it) }
        return taskJsonList.joinToString(prefix = "[", postfix = "]", separator = ",")
    }

    companion object {
        // Deserialize TaskList from JSON
        fun fromJson(json: String): TaskList {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter<List<Task>>(Types.newParameterizedType(List::class.java, Task::class.java))
            val taskList = TaskList()
            val taskArray = adapter.fromJson(json)
            if (taskArray != null) {
                taskList.tsks.addAll(taskArray)
            }
            return taskList
        }
    }
}

fun main() {
    val jsonFile = File("tasklist.json")
    val tskList=
    if (jsonFile.exists())  TaskList.fromJson(jsonFile.readText())
    else TaskList()
    tskList.doActions()
}


