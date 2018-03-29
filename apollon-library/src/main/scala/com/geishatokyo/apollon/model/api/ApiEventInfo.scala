package com.geishatokyo.apollon.model.api

import java.util.Date
import org.joda.time._
import com.geishatokyo.apollon.annotation.InfoType
import com.geishatokyo.apollon.model._


//@hold[import]
import com.geishatokyo.apollon.battle.ApiEvent
import com.geishatokyo.apollon.battle.ApiEvent._
//@end

// Do not delete 'hold' and 'replace'.
// These are the guide for code generation.

/**
 * ApiEventInfo
 */
@InfoType("ApiEventInfo")
trait ApiEventInfo
  extends InfoBase
  //@hold[extends]
//@end
{

    def id : Int
    def eventType : String

    //@hold[inner-trait]

    //@end
}

//@hold[free-space]

//@end
      