// my rules
var proxy_cntlm = "PROXY localhost:9998";
//var proxy_tunnel = proxy_cntlm;
var proxy_tunnel = "SOCKS localhost:9990";

function FindProxyRoute(ip, fqn, port){
        if( shExpMatch(fqn, "*.mydomain.net")
                || shExpMatch(ip, "10.*")
        )
			return "DIRECT";
        
       
        if( port == 80 || port == 8080 ){
			return proxy_tunnel;
        } else if( shExpMatch(fqn, "*.google.*") 
        		|| shExpMatch(fqn, "*.ggpht.com") 
        		|| shExpMatch(fqn, "*.alfresco.com")
        		|| shExpMatch(fqn, "*.yahoo.com")
        	) {
			return proxy_cntlm;
        } else {
			return proxy_tunnel;
		}
}
