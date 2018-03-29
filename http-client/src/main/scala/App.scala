import com.geishatokyo.httpclient.Client

/**
 * 
 * User: takeshita
 * DateTime: 12/12/13 14:42
 */
object App {

  def main(args : Array[String]) = {

    if (args.length > 0){
      Client.baseUrl = args(0)
    }

  }
}
