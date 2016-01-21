/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.hadoop.config.client.helper;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationDeleteRequest;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationDeleteResponse;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationSubmissionRequest;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationSubmissionResponse;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationUpdateRequest;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationUpdateResponse;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.QueueUserACLInfo;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.security.AMRMTokenIdentifier;

public class DelegatingYarnClient extends YarnClient {

  private final YarnClient client;
  private final UgiWrapper ugi;

  public DelegatingYarnClient(YarnClient client, UgiWrapper ugi) {
    super(DelegatingYarnClient.class.getName());
    this.client = client;
    this.ugi = ugi;
  }

  @Override
  public void init(Configuration conf) {
    client.init(conf);
  }

  @Override
  public void start() {
    client.start();
  }

  @Override
  public void stop() {
    client.stop();
  }

  @Override
  public synchronized Configuration getConfig() {
    return client.getConfig();
  }

  @Override
  public YarnClientApplication createApplication() throws YarnException, IOException {
    return ugi.doAs(() -> client.createApplication());
  }

  @Override
  public ApplicationId submitApplication(ApplicationSubmissionContext applicationSubmissionContext)
      throws YarnException, IOException {
    return ugi.doAs(
        () -> client.submitApplication(applicationSubmissionContext));
  }

  @Override
  public void killApplication(ApplicationId applicationId) throws YarnException, IOException {
    ugi.doAs(() -> {
      client.killApplication(applicationId);
      return null;
    });
  }

  @Override
  public ApplicationReport getApplicationReport(ApplicationId applicationId)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplicationReport(applicationId));
  }

  @Override
  public Token<AMRMTokenIdentifier> getAMRMToken(ApplicationId applicationId)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getAMRMToken(applicationId));
  }

  @Override
  public List<ApplicationReport> getApplications() throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplications());
  }

  @Override
  public List<ApplicationReport> getApplications(Set<String> set)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplications(set));
  }

  @Override
  public List<ApplicationReport> getApplications(EnumSet<YarnApplicationState> enumSet)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplications(enumSet));
  }

  @Override
  public List<ApplicationReport> getApplications(Set<String> set,
      EnumSet<YarnApplicationState> enumSet) throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplications(set, enumSet));
  }

  @Override
  public List<ApplicationReport> getApplications(Set<String> set, Set<String> set1,
      Set<String> set2, EnumSet<YarnApplicationState> enumSet) throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplications(set, set1, set2, enumSet));
  }

  @Override
  public YarnClusterMetrics getYarnClusterMetrics() throws YarnException, IOException {
    return ugi.doAs(() -> client.getYarnClusterMetrics());
  }

  @Override
  public List<NodeReport> getNodeReports(NodeState... nodeStates)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getNodeReports(nodeStates));
  }

  @Override
  public org.apache.hadoop.yarn.api.records.Token getRMDelegationToken(Text text)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getRMDelegationToken(text));
  }

  @Override
  public QueueInfo getQueueInfo(String s) throws YarnException, IOException {
    return ugi.doAs(() -> client.getQueueInfo(s));
  }

  @Override
  public List<QueueInfo> getAllQueues() throws YarnException, IOException {
    return ugi.doAs(() -> client.getAllQueues());
  }

  @Override
  public List<QueueInfo> getRootQueueInfos() throws YarnException, IOException {
    return ugi.doAs(() -> client.getRootQueueInfos());
  }

  @Override
  public List<QueueInfo> getChildQueueInfos(String s) throws YarnException, IOException {
    return ugi.doAs(() -> client.getChildQueueInfos(s));
  }

  @Override
  public List<QueueUserACLInfo> getQueueAclsInfo() throws YarnException, IOException {
    return ugi.doAs(() -> client.getQueueAclsInfo());
  }

  @Override
  public ApplicationAttemptReport getApplicationAttemptReport(
      ApplicationAttemptId applicationAttemptId) throws YarnException, IOException {
    return ugi.doAs(
        () -> client.getApplicationAttemptReport(applicationAttemptId));
  }

  @Override
  public List<ApplicationAttemptReport> getApplicationAttempts(ApplicationId applicationId)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getApplicationAttempts(applicationId));
  }

  @Override
  public ContainerReport getContainerReport(ContainerId containerId)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getContainerReport(containerId));
  }

  @Override
  public List<ContainerReport> getContainers(ApplicationAttemptId applicationAttemptId)
      throws YarnException, IOException {
    return ugi.doAs(() -> client.getContainers(applicationAttemptId));
  }

  @Override
  public void moveApplicationAcrossQueues(ApplicationId applicationId, String s)
      throws YarnException, IOException {
    ugi.doAs(() -> {
      client.moveApplicationAcrossQueues(applicationId, s);
      return null;
    });
  }

  @Override
  public ReservationSubmissionResponse submitReservation(
      ReservationSubmissionRequest reservationSubmissionRequest) throws YarnException, IOException {
    return ugi.doAs(
        () -> client.submitReservation(reservationSubmissionRequest));
  }

  @Override
  public ReservationUpdateResponse updateReservation(
      ReservationUpdateRequest reservationUpdateRequest) throws YarnException, IOException {
    return ugi.doAs(() -> client.updateReservation(reservationUpdateRequest));
  }

  @Override
  public ReservationDeleteResponse deleteReservation(
      ReservationDeleteRequest reservationDeleteRequest) throws YarnException, IOException {
    return ugi.doAs(() -> client.deleteReservation(reservationDeleteRequest));
  }

  @Override
  public Map<NodeId, Set<String>> getNodeToLabels() throws YarnException, IOException {
    return ugi.doAs(() -> client.getNodeToLabels());
  }

  @Override
  public Set<String> getClusterNodeLabels() throws YarnException, IOException {
    return ugi.doAs(() -> client.getClusterNodeLabels());
  }

}
