package com.oschrenk.gwatch

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit

object Formatters {
   val default: (RevCommit) => String = (rev: RevCommit) => {
     rev.toString
   }
}

object Cli extends App {

  import scala.collection.JavaConverters._

  val workingDir = new File(".").getAbsolutePath
  val repoDir = new File(workingDir, ".git").getCanonicalFile

  val repo = new FileRepositoryBuilder().setGitDir(repoDir)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir()      // scan up the file system tree
    .build()
  val git = new Git(repo)

  println("Having repository: " + repo.getDirectory)

  // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
  val head = repo.exactRef("refs/heads/master")
  println("Ref of refs/heads/master: " + head)

  val branches: Seq[Ref] = asScalaBuffer(git.branchList().call())
  branches.foreach { ref =>
    println(s"Branch: ${ref.getName} ${ref.getObjectId.getName}")
  }

  val commits = git.log().all().call().asScala
  val format = Formatters.default
  commits.foreach { rev =>
    println(format(rev))
  }

}
