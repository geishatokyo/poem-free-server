package com.geishatokyo.apollon.error

//@hold[import]
//@end

/**
 * Auto generated
 */
object ErrorCode {
  def Success(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.Success, "Success", params : _*)

  def IncidentApi(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.IncidentApi, "IncidentApi", params : _*)

  def IncidentApiClientLevel(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.IncidentApiClientLevel, "IncidentApiClientLevel", params : _*)

  def IncidentApiServerLevel(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.IncidentApiServerLevel, "IncidentApiServerLevel", params : _*)

  def ServiceClosed(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.ServiceClosed, "ServiceClosed", params : _*)

  def ExpectOfUnReachedCode(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.ExpectOfUnReachedCode, "ExpectOfUnReachedCode", params : _*)

  def Unimplemented(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.Unimplemented, "Unimplemented", params : _*)

  def BadDbData(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.BadDbData, "BadDbData", params : _*)

  def BadEnumType(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.BadEnumType, "BadEnumType", params : _*)

  def RejectUnsafe(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.RejectUnsafe, "RejectUnsafe", params : _*)

  def BadClientData(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.BadClientData, "BadClientData", params : _*)

  def WrongJsonFormat(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.WrongJsonFormat, "WrongJsonFormat", params : _*)

  def Unauthorized(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.Unauthorized, "Unauthorized", params : _*)

  def Banned(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.Banned, "Banned", params : _*)

  def UnacceptableVersion(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.UnacceptableVersion, "UnacceptableVersion", params : _*)

  def UserNotFound(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.UserNotFound, "UserNotFound", params : _*)

  def DuplicateIds(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.DuplicateIds, "DuplicateIds", params : _*)

  def ContainNGWord(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.ContainNGWord, "ContainNGWord", params : _*)

  def WrongTextLength(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.WrongTextLength, "WrongTextLength", params : _*)

  def BadRangeNumber(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.BadRangeNumber, "BadRangeNumber", params : _*)

  def UnknownError(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.UnknownError, "UnknownError", params : _*)

  def PairingRequestTimeOut(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.PairingRequestTimeOut, "PairingRequestTimeOut", params : _*)

  def OpponentParingAcceptTimeOut(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.OpponentParingAcceptTimeOut, "OpponentParingAcceptTimeOut", params : _*)

  def JoinTimeOut(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.JoinTimeOut, "JoinTimeOut", params : _*)

  def NotPairingBattle(params : (String,Any)*) =
    makeValidationException(ErrorCodeDef.NotPairingBattle, "NotPairingBattle", params : _*)

  def OverJoinRoom(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.OverJoinRoom, "OverJoinRoom", params : _*)

  def BattleRoomNotFoundByRoomCode(params : (String,Any)*) =
    makeRegularException(ErrorCodeDef.BattleRoomNotFoundByRoomCode, "BattleRoomNotFoundByRoomCode", params : _*)

  def CantCreateRoom(params : (String,Any)*) =
    makeServerException(ErrorCodeDef.CantCreateRoom, "CantCreateRoom", params : _*)


  //@hold[additional-code]

  def UnknownError(e : Throwable, params : (String,Any)*) = {
    val err = ServerException(ErrorCodeDef.UnknownError,"Unknown error", params : _*)
    err.initCause(e)
    err
  }

  //@end

  def makeServerException(code : Int, message : String, params : (String,Any)*) = {
    ServerException(code, message, params : _*)
  }

  def makeValidationException(code : Int, message : String, params : (String,Any)*) = {
    ValidationException(code, message, params : _*)
  }

  def makeRegularException(code : Int, message : String, params : (String,Any)*) = {
    RegularException(code, message, params : _*)
  }
}
