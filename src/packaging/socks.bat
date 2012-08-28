SET PORT=9999
SET IFACE=0.0.0.0
SET JSFILE=customRules.js
REM SET JVM_OPTS_MEM=-Xmx20m -XX:MaxPermSize=16m -XX:ReservedCodeCacheSize=4m -XX:+UseG1GC
start javaw -Dnetworkaddress.cache.ttl=900 -cp log4j-1.2.15.jar;socksProxy.jar sp.SocksServer %PORT% %IFACE% %JSFILE%
