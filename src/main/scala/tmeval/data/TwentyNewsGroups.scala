package tmeval.data

import java.io._


/**
 * Extract the files from the twenty news groups archive. First tries to
 * do it by using direct OS call to the tar command. If that fails, it backs
 * off to slower method using VFS.
 *
 * @author jasonbaldridge
 */
object TwentyNewsGroupsPreparer {

  import org.apache.commons.vfs2.{VFS,FileObject,AllFileSelector}
  import tmeval.Constants.TMEVAL_DIR

  val tmevalDir = new File(TMEVAL_DIR)
  
  val extractDir = new File(tmevalDir, "data/extracted")
  val outputDir = new File(extractDir, "20news")

  def main(args: Array[String]) {
    extract()
  }

  def extract() {
    
    println("Extracting 20 News Groups data.")

    import scala.sys.process._

    extractDir.mkdirs
    val newsFile = tmevalDir.getAbsolutePath + "/data/orig/20news.tar.bz2"
    
    val success = Seq("tar", "-xjf", newsFile, "-C", extractDir.getAbsolutePath).!
    if (success==0) {
      // Get rid of the readme so that it isn't part of the documents used for learning
      // topics.
      new File(outputDir, "README.md").delete
      println("20 news groups data successfully extracted to " + outputDir.getAbsolutePath)
    } else {
      println("Unable to extract 20 news groups data using operating system. Backing"
              + "off to a slower method that should work. Go get some coffee---it will"
              + "take quite some time.")
              
      outputDir.mkdirs
      val fileDescr = "tbz2:file:/" + tmevalDir.getAbsolutePath + "/data/orig/20news.tar.bz2"

      val manager = VFS.getManager
      val topDir = manager.resolveFile(fileDescr).getChild("20news")
      
      println("Extracting 20 newsgroups data. This will take a while.")
      println("Extracting training directory.")
      val trainOutput = new File(outputDir, "train")
      manager.toFileObject(trainOutput)
        .copyFrom(topDir.getChild("train"), new AllFileSelector)

      println("Extracting testing directory.")
      val evalOutput = new File(outputDir, "eval")
      manager.toFileObject(evalOutput)
        .copyFrom(topDir.getChild("eval"), new AllFileSelector)

      topDir.close
    }
  }

}
