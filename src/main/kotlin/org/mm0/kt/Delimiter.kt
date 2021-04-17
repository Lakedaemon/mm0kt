package org.mm0.kt

sealed class Delimiter(val key:String){
    class Left(key:String):Delimiter(key)
    class Both(key:String):Delimiter(key)
    class Right(key:String):Delimiter(key)
}