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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationDeleteRequest;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationDeleteResponse;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationSubmissionRequest;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationSubmissionResponse;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationUpdateRequest;
import org.apache.hadoop.yarn.api.protocolrecords.ReservationUpdateResponse;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.ReservationDeleteRequestPBImpl;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.ReservationDeleteResponsePBImpl;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.ReservationSubmissionRequestPBImpl;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.ReservationSubmissionResponsePBImpl;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.ReservationUpdateRequestPBImpl;
import org.apache.hadoop.yarn.api.protocolrecords.impl.pb.ReservationUpdateResponsePBImpl;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.QueueUserACLInfo;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationAttemptIdPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationAttemptReportPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationIdPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationReportPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationSubmissionContextPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ContainerIdPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ContainerReportPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.QueueInfoPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.TokenPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.YarnClusterMetricsPBImpl;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.security.AMRMTokenIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelegatingYarnClientTest {

  @Mock
  private YarnClient client;

  @Mock
  private UgiWrapper ugi;

  private DelegatingYarnClient objectUnderTest;

  private ArgumentCaptor<UgiWrapper.YarnPrivilegedAction> argumentCaptor;

  @Before
  public void setup() {
    objectUnderTest = new DelegatingYarnClient(client, ugi);
    argumentCaptor = ArgumentCaptor.forClass(UgiWrapper.YarnPrivilegedAction.class);
  }

  private <T> void verifyMockConsumerCorrectness(T mock,
      UgiWrapper.YarnPrivilegedAction actualConsumer, MockVerification<T> mockVerification)
          throws Exception {
    verifyNoMoreInteractions(mock);
    actualConsumer.run();
    mockVerification.verify(mock);
  }

  @Test
  public void createApplication_lambdaPassedToFunction_invokeWithoutException() throws Exception {

    YarnClientApplication expectedApplication = new YarnClientApplication(null, null);


    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedApplication);

    YarnClientApplication actualApplication = objectUnderTest.createApplication();

    assertThat(actualApplication, equalTo(expectedApplication));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).createApplication());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void submitApplication_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationId expectedAppID = new ApplicationIdPBImpl();

    ApplicationSubmissionContextPBImpl context = new ApplicationSubmissionContextPBImpl();
    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedAppID);

    ApplicationId actualAppID = objectUnderTest.submitApplication(context);

    assertThat(actualAppID, equalTo(expectedAppID));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).submitApplication(context));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void killApplication_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationId appID = new ApplicationIdPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(null);
    objectUnderTest.killApplication(appID);

    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).killApplication(appID));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getApplicationReport_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationReportPBImpl expectedAppReport = new ApplicationReportPBImpl();
    ApplicationId appID = new ApplicationIdPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedAppReport);

    ApplicationReport actualAppReport = objectUnderTest.getApplicationReport(appID);

    assertThat(actualAppReport, equalTo(expectedAppReport));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getApplicationReport(appID));

    verifyNoMoreInteractions(client, ugi);
  }


  @Test
  public void getAMRMToken_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationId appID = new ApplicationIdPBImpl();
    Token<AMRMTokenIdentifier> expectedToken = new Token<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedToken);

    Token<AMRMTokenIdentifier> actualToken = objectUnderTest.getAMRMToken(appID);

    assertThat(actualToken, equalTo(expectedToken));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getAMRMToken(appID));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getApplications_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    List<ApplicationReport> expectedList = new ArrayList<>();
    expectedList.add(new ApplicationReportPBImpl());

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<ApplicationReport> actualList = objectUnderTest.getApplications();

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getApplications());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getApplications_lambdaPassedToFunction_invokeWithSet() throws Exception {
    List<ApplicationReport> expectedList = new ArrayList<>();
    expectedList.add(new ApplicationReportPBImpl());
    Set<String> set = new HashSet<>();
    set.add("test");

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<ApplicationReport> actualList = objectUnderTest.getApplications(set);

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getApplications(set));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getYarnClusterMetrics_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    YarnClusterMetrics expectedMetrics = new YarnClusterMetricsPBImpl();
    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedMetrics);

    YarnClusterMetrics actualMetrics = objectUnderTest.getYarnClusterMetrics();

    assertThat(actualMetrics, equalTo(expectedMetrics));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getYarnClusterMetrics());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getNodeReports_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    List<NodeReport> expectedList = new ArrayList<>();
    NodeState[] states = new NodeState[0];

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<NodeReport> actualList = objectUnderTest.getNodeReports(states);

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getNodeReports(states));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getRMDelegationToken_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    org.apache.hadoop.yarn.api.records.Token expectedToken = new TokenPBImpl();
    expectedToken.setKind("potato");
    expectedToken.setIdentifier(Text.encode("potato"));
    expectedToken.setPassword(Text.encode("potato"));
    expectedToken.setService("potato");

    Text text = new Text("ok");
    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedToken);

    org.apache.hadoop.yarn.api.records.Token actualToken =
        objectUnderTest.getRMDelegationToken(text);

    assertThat(actualToken, equalTo(expectedToken));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getRMDelegationToken(text));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getQueueInfo_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    String s = "potato";
    QueueInfo expectedInfo = new QueueInfoPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedInfo);

    QueueInfo actualInfo = objectUnderTest.getQueueInfo(s);

    assertThat(actualInfo, equalTo(expectedInfo));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getQueueInfo(s));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getAllQueues_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    List<QueueInfo> expectedList = new ArrayList<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<QueueInfo> actualList = objectUnderTest.getAllQueues();

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getAllQueues());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getRootQueueInfos_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    List<QueueInfo> expectedList = new ArrayList<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<QueueInfo> actualList = objectUnderTest.getRootQueueInfos();

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getRootQueueInfos());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getChildQueueInfos_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    List<QueueInfo> expectedList = new ArrayList<>();
    String s = "potato";

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<QueueInfo> actualList = objectUnderTest.getChildQueueInfos(s);

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getChildQueueInfos(s));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getQueueAclsInfo_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    List<QueueUserACLInfo> expectedList = new ArrayList<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedList);

    List<QueueUserACLInfo> actualList = objectUnderTest.getQueueAclsInfo();

    assertThat(actualList, equalTo(expectedList));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getQueueAclsInfo());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getApplicationAttemptReport_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationAttemptId applicationAttemptId = new ApplicationAttemptIdPBImpl();
    ApplicationAttemptReportPBImpl expectedItem = new ApplicationAttemptReportPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    ApplicationAttemptReport actualItem =
        objectUnderTest.getApplicationAttemptReport(applicationAttemptId);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getApplicationAttemptReport(applicationAttemptId));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getApplicationAttempts_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationId appId = new ApplicationIdPBImpl();
    List<ApplicationAttemptReport> expectedItem = new ArrayList<>();
    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    List<ApplicationAttemptReport> actualItem = objectUnderTest.getApplicationAttempts(appId);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getApplicationAttempts(appId));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getContainerReport_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ContainerId containerId = new ContainerIdPBImpl();
    ContainerReport expectedItem = new ContainerReportPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    ContainerReport actualItem = objectUnderTest.getContainerReport(containerId);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getContainerReport(containerId));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getContainers_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationAttemptId applicationAttemptId = new ApplicationAttemptIdPBImpl();
    List<ContainerReport> expectedItem = new ArrayList<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    List<ContainerReport> actualItem = objectUnderTest.getContainers(applicationAttemptId);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getContainers(applicationAttemptId));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void moveApplicationAcrossQueues_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ApplicationId applicationId = new ApplicationIdPBImpl();
    String s = "potato";

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(null);

    objectUnderTest.moveApplicationAcrossQueues(applicationId, s);

    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).moveApplicationAcrossQueues(applicationId, s));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void submitReservation_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ReservationSubmissionRequest reservationSubmissionRequest =
        new ReservationSubmissionRequestPBImpl();

    ReservationSubmissionResponse expectedItem = new ReservationSubmissionResponsePBImpl();


    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    ReservationSubmissionResponse actualItem =
        objectUnderTest.submitReservation(reservationSubmissionRequest);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).submitReservation(reservationSubmissionRequest));

    verifyNoMoreInteractions(client, ugi);

  }

  @Test
  public void updateReservation_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ReservationUpdateResponse expectedItem = new ReservationUpdateResponsePBImpl();
    ReservationUpdateRequest reservationUpdateRequest = new ReservationUpdateRequestPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    ReservationUpdateResponse actualItem =
        objectUnderTest.updateReservation(reservationUpdateRequest);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).updateReservation(reservationUpdateRequest));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void deleteReservation_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    ReservationDeleteResponse expectedItem = new ReservationDeleteResponsePBImpl();
    ReservationDeleteRequest reservationDeleteRequest = new ReservationDeleteRequestPBImpl();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    ReservationDeleteResponse actualItem =
        objectUnderTest.deleteReservation(reservationDeleteRequest);

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).deleteReservation(reservationDeleteRequest));

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getNodeToLabels_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    Map<NodeId, Set<String>> expectedItem = new HashMap<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    Map<NodeId, Set<String>> actualItem = objectUnderTest.getNodeToLabels();

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getNodeToLabels());

    verifyNoMoreInteractions(client, ugi);
  }

  @Test
  public void getClusterNodeLabels_lambdaPassedToFunction_invokeWithoutException() throws Exception {
    Set<String> expectedItem = new HashSet<>();

    when(ugi.doAs(argumentCaptor.capture())).thenReturn(expectedItem);

    Set<String> actualItem = objectUnderTest.getClusterNodeLabels();

    assertThat(actualItem, equalTo(expectedItem));
    verify(ugi).doAs(any());

    UgiWrapper.YarnPrivilegedAction lambdaPassedToUgiDoAs = argumentCaptor.getValue();

    verifyThatMethod(lambdaPassedToUgiDoAs).whenCalledOn(client)
        .willDoThis(x -> verify(x).getClusterNodeLabels());

    verifyNoMoreInteractions(client, ugi);
  }

  private interface MockVerification<T> {
    void verify(T mock) throws Exception;
  }

  private LambdaVerifyBuilder verifyThatMethod(UgiWrapper.YarnPrivilegedAction actualConsumer) {
    return new LambdaVerifyBuilder(actualConsumer);
  }

  private class LambdaVerifyBuilder {
    private final UgiWrapper.YarnPrivilegedAction actualConsumer;

    public LambdaVerifyBuilder(UgiWrapper.YarnPrivilegedAction actualConsumer) {

      this.actualConsumer = actualConsumer;
    }

    public <T> LambdaVerifyBuilderSecondStep<T> whenCalledOn(T mock) {
      return new LambdaVerifyBuilderSecondStep<>(actualConsumer, mock);
    }

    private class LambdaVerifyBuilderSecondStep<T> {
      private final UgiWrapper.YarnPrivilegedAction actualConsumer;
      private final T mock;

      public LambdaVerifyBuilderSecondStep(UgiWrapper.YarnPrivilegedAction actualConsumer, T mock) {
        this.actualConsumer = actualConsumer;
        this.mock = mock;
      }

      public void willDoThis(MockVerification<T> mockVerification) throws Exception {
        verifyMockConsumerCorrectness(mock, actualConsumer, mockVerification);
      }
    }
  }
}
