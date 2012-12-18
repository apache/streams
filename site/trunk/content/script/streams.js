function init() {
   var tables=document.getElementsByTagName("table");
   for(var i=0; i<tables.length; i++) {
      tables[i].className += "table table-striped";
   }
}

if (window.addEventListener) {
    window.addEventListener('load', init, false)
} else if (window.attachEvent) {
    window.attachEvent('onload', init);
}
