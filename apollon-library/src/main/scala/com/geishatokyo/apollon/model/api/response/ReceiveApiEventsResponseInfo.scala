package com.geishatokyo.apollon.model.api.response

import java.util.Date
import org.joda.time._
import com.geishatokyo.apollon.annotation.InfoType
import com.geishatokyo.apollon.model._

import com.geishatokyo.apollon.model.api._

//@hold[import]
//@end

// Do not delete 'hold' and 'replace'.
// These are the guide for code generation.

/**
 * ReceiveApiEventsResponseInfo
 */
@InfoType("ReceiveApiEventsResponseInfo")
case class ReceiveApiEventsResponseInfo(
  apiEvents : List[ApiEventInfo],
  lastApiEventId : Long
) extends ResponseInfoBase
//@hold[extends]
//@end
{


  //@hold[inner-class]

  //@end

}

//@hold[free-space]

//@end
      