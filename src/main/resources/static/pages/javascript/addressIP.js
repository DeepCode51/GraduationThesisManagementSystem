const ipAddress = "http://localhost:9199/";

function errorHandle(error) {
    if("loginAgain" == error.responseText){
        localStorage.clear();
        sessionStorage.clear();
        localStorage.setItem("appstate","timeout");
        window.parent.location.href='login.html';
    }else{
        alert("请检查您的网络状况，或者重新登陆");
    }
}

//修改日历框的显示格式
function formatter(date){
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var fen = date.getMinutes();
    var second = date.getSeconds();
    month = month < 10 ? '0' + month : month;
    day = day < 10 ? '0' + day : day;
    hour = hour < 10 ? '0' + hour : hour;
    fen = fen < 10 ? '0' + fen : fen;
    second = second < 10 ? '0' + second : second;
    return year + "-" + month + "-" + day + " " + hour+":"+fen+":"+second;
}

function parser(s){
    var t = Date.parse(s);
    if (!isNaN(t)){
        return new Date(t);
    } else {
        return new Date();
    }
}