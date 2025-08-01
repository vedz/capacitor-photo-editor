package fr.binova.capacitor.photoeditor

import java.io.IOException

sealed interface SaveFileResult {

    object Success : SaveFileResult
    class Failure(val exception: IOException) : SaveFileResult

}