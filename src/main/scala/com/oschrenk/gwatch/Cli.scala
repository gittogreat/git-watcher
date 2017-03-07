package com.oschrenk.gwatch

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;

object Cli extends App {

  val workingDir = new File(".").getAbsolutePath()
  val repoDir = new File(workingDir, ".git").getCanonicalFile()

  val repository = new FileRepositoryBuilder().setGitDir(repoDir)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir()      // scan up the file system tree
    .build()

  println("Having repository: " + repository.getDirectory());

  // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
  val head = repository.exactRef("refs/heads/master");
  println("Ref of refs/heads/master: " + head);
}
