function initLightbox() {
	$('a.lightbox').lightBox();
	$('div.multiShot a').lightBox({fixedNavigation:true});
}

function onBefore() { 
    $('#feature').fadeOut(300);
} 
function onAfter() { 
    $('#feature').html(this.alt).fadeIn(50);
}

$(document).ready(function() {	

    $('.slideshow').cycle({
    		fx: 'scrollLeft',
            speed:    500, 
            timeout:  4000,
            before:  onBefore, 
            after:   onAfter            
    	});
    	
    $("a.divButton").button();
	
	$("#tabs").tabs();
	$('#featurestab').tabs().addClass('ui-tabs-vertical ui-helper-clearfix');
	$('#featurestab li').removeClass('ui-corner-top').addClass('ui-corner-left');				

});