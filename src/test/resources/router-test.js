
function FindProxyRoute(ip, fqn, port){
	println("js: "+ip+" "+fqn+" "+port);
	if(shExpMatch( ip, "192.168.*")) {
		return "start";
	}
	if(shExpMatch( ip, "*.1.1")) {
		return "end";
	}
	return FindProxyForURL("http://test/", "test");
}

function FindProxyForURL(url, host){
	return "none";
	
}
