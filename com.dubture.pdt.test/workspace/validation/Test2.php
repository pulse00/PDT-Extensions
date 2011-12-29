<?php 

namespace Acme\Demo;

interface Bar {
	
	function x();
	
	function y();
	
}


namespace Frenzn\Demo\Controller;


class Controller implements Acme\Demo\Bar {
	
	public function x() {
	
	}
	
	public function y() {
	
	
	}		
}