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
 * OperationApiEventInfo
 */
@InfoType("OperationApiEventInfo")
case class OperationApiEventInfo(
  id : Int,
  eventType : String,
  operation : String
) extends ApiEventInfo
//@hold[extends]
//@end
{


  //@hold[inner-class]

  //@end

}

//@hold[free-space]

//@end
      