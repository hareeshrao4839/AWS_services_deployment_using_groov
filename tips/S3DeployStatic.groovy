#!/usr/bin/env groovy

// Puropse: Deploy static websites to S3
def DeployToS3(region, bucket, repo, archive, source_object) {
    def bucket_url = "s3://${bucket}/"
    // Download build from repository bucket first
    withAWS(region: region) {
        s3Download(
            file: archive, bucket: repo,
            path: source_object, force: true
        )
    }
    withAWS(region: region) {
        // Unarchive build
        sh "mkdir -p s3dist"
        sh "tar xzf ${archive} -C s3dist"
        // Sync the static with S3 bucket
        dir ('s3dist') {
            sh """aws s3 sync --region ${region} --delete \
                --acl=public-read . ${bucket_url}""".stripIndent()
        }
    }
}

stage ('Deploy to S3') {
    aws_region = 'us-east-1'
	bucket = 'example.com'
	repo_bucket = 'mybuilds-us'
    // You might pick this parameter generated by Jenkins
	deploy_archive = 'example.com-master.tar.gz'
	source_object = 'example-builds/${deploy_archive}'
	aws.DeployToS3(
		aws_region, bucket, repo_bucket, deploy_archive, source_object
	)
}
