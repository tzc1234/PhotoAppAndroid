package com.tszlung.photoapp.main.helpers

import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result

fun successResult(data: ByteArray) = Result.Success<ByteArray, Error>(data)
fun failuresResult() = Result.Failure<ByteArray, Error>(AnyError.ANY)