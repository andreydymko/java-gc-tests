@echo off
setlocal EnableDelayedExpansion
echo starting tests;
echo:

REM путь до исполняемого файла
set jarDir=target/javaGcTest-1.0.jar

REM пути до исполняемых файлов различных JVM
set javaPath[0]=C:\Programs64\Java\jre1.8.0_321\bin\java.exe
set javaPath[1]=C:\Programs64\Java\redhat-openjdk-1.8.0.312-2.b07\bin\java.exe
set javaPath[2]=C:\Programs64\Java\openj9-jdk8u312-b07\bin\java.exe

REM названия этих JVM
set jvmName[0]=oracle
set jvmName[1]=openjdk
set jvmName[2]=openj9

REM список сборщиков мусора, которые будут использоваться
set collector[0]=-XX:+UseParallelGC
set collector[1]=-XX:+UseG1GC
set collector[2]=-XX:+UseConcMarkSweepGC

REM названия этих сборщиков мусора
set collectorName[0]=ParallelGC
set collectorName[1]=G1GC
set collectorName[2]=CMS

REM шаг (в итоговом количестве выделенной памяти в мегабайтах), с которым будет производится запись результатов
set step[0]=64
set step[1]=1024

REM размер кучи для JVM
set heap[0]=256m
set heap[1]=4096m

REM итоговое количество памяти, которое выделит тест через оператор new, в мегабайтах
set size[0]=327680
set size[1]=327680

REM типы запуска тестов:
REM normal - всё общее количество памяти выделяется и высвобождается в одном потоке
REM parallel - общее количество памяти на выделение разбивается между потоками, количество потоков равно количеству виртуальных процессоров
set runType[0]=normal
set runType[1]=parallel

for %%h in (0 1) do (
    set hsize=!heap[%%h]!
    set dsize=!size[%%h]!
    set sstep=!step[%%h]!
    for %%j in (0 1 2) do (
        set jname=!jvmName[%%j]!
        set jpath=!javaPath[%%j]!
        for %%c in (0 1 2) do (
            set cname=!collectorName[%%c]!
            set ctype=!collector[%%c]!
            for %%r in (0 1) do (
                set rtype=!runType[%%r]!

                set path="!dsize!_!rtype!_!hsize!_!jname!_!cname!"
                if not exist "!path!" mkdir !path!
                set args=-Xms!hsize! -Xmx!hsize! !ctype! -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:!path!/gclog.log

                echo directory is !path!
                echo args are !args!
                echo:

                !jpath! -jar !args! !jarDir! !dsize! !sstep! false !rtype! !path!
            )
        )
    )
)
echo finished successfully;
