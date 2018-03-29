package com.geishatokyo.apollon.model.api

import java.util.Date
import org.joda.time._
import com.geishatokyo.apollon.annotation.InfoType
import com.geishatokyo.apollon.model._


//@hold[import]
//@end

// Do not delete 'hold' and 'replace'.
// These are the guide for code generation.

/**
 * LeaveApiEventInfo
 */
@InfoType("LeaveApiEventInfo")
case class LeaveApiEventInfo(
  id : Int,
  eventType : String,
  reason : String
) extends ApiEventInfo
//@hold[extends]
//@end
{


  //@hold[inner-class]

  //@end

}

//@hold[free-space]

//@end
      