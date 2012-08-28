@echo off
REM Besoin de java 6 pour le systray
REM PATH="%JAVA7_HOME%\bin\";%PATH%

SET PORT=9999
SET IFACE=0.0.0.0
SET JSFILE=customRules.js
SET JVM_OPTS_MEM=-Xmx20m -XX:MaxPermSize=16m -XX:ReservedCodeCacheSize=4m -XX:+UseG1GC
start javaw -server %JVM_OPTS_MEM% -Dnetworkaddress.cache.ttl=900 -cp log4j-1.2.15.jar;socksProxy.jar sp.gui.SocksGui %PORT% %IFACE% %JSFILE%
