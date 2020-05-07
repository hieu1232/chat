package hieusenpaj.com.whapp.objet

class Message (var key:String,
               var from:String,
               var message:String,
               var type:String,
               var isseen:String,
               var time:String,
               var date:String){
    constructor():this("","","","","","","")
}